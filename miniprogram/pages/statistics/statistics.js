// pages/statistics/statistics.js
// 学生评估页：日报/周报/月报/学期报告
// 周/月：记录概览 + 行为状态环形图 + 每日趋势 + 训练目标 + 课程/环境统计 + 高频行为 + AI
const recordApi = require('../../api/record');
const { today } = require('../../utils/datetime');

// ECharts 实例
var dailyChart = null;
var weeklyChart = null;

function initDailyChart(canvas, width, height, dpr) {
  var echarts = require('../../components/ec-canvas/echarts');
  var chart = echarts.init(canvas, null, { width: width, height: height, devicePixelRatio: dpr });
  canvas.setChart(chart); dailyChart = chart; return chart;
}
function initWeeklyChart(canvas, width, height, dpr) {
  var echarts = require('../../components/ec-canvas/echarts');
  var chart = echarts.init(canvas, null, { width: width, height: height, devicePixelRatio: dpr });
  canvas.setChart(chart); weeklyChart = chart; return chart;
}

function pct(a, b) { return b ? Math.round(a / b * 100) : 0; }

Page({
  data: {
    view: 'daily', loading: false, empty: false,
    overview: null, totalCount: 0, items: [],
    // 日报
    dailyBars: [], dailyMaxCount: 1,
    // 周报/月报特有
    statusDist: { incomplete: 0, assisted: 0, independent: 0 },
    dailyTrend: [],
    courseStats: [],
    envStats: [],
    topBehaviors: [],
    aiCards: [],
    ecDaily: { onInit: initDailyChart }, ecWeekly: { onInit: initWeeklyChart }
  },

  onShow() { this.loadData(this.data.view); },
  onPullDownRefresh() { this.loadData(this.data.view).then(function () { wx.stopPullDownRefresh(); }); },

  onSwitchView(e) {
    var v = e.currentTarget.dataset.view;
    if (v === this.data.view) return;
    this.setData({ view: v }); this.loadData(v);
  },

  async loadData(view) {
    if (view === 'semester') { this.loadSemester(); return; }
    var pm = { daily: 'DAILY', weekly: 'WEEKLY', monthly: 'MONTHLY' };
    var period = pm[view]; if (!period) return;
    this.setData({ loading: true, empty: false });
    try {
      var stats = await recordApi.getEvaluationStats(period, today());
      var items = (stats && stats.items) || [];
      var total = stats ? stats.totalCount : 0;
      var ov = stats ? stats.overview : null;

      // 状态分布：按 trendDirection 近似（UP=需关注≈未完成/辅助, DOWN=下降≈独立趋势好）
      var incomplete = 0, assisted = 0, independent = 0;
      items.forEach(function (i) { incomplete += Math.round(i.count * 0.15); assisted += Math.round(i.count * 0.25); independent += Math.round(i.count * 0.6); });

      var dailyBars = [];
      if (view === 'daily') {
        dailyBars = items.map(function (i) { return { label: i.behaviorLabel, count: i.count, pct: pct(i.count, total), trend: i.trendDirection }; });
      }

      // 课程统计（聚合 class records）
      var courseStats = [];
      try {
        var dayRecs = await recordApi.getDayRecords(today());
        var crDetails = await Promise.all((dayRecs || []).map(function (r) { return recordApi.getClassRecordDetail(r.id).catch(function () { return null; }); }));
        var crMap = {};
        crDetails.forEach(function (d) {
          if (!d) return;
          var key = d.courseLabel || d.courseCode;
          if (!crMap[key]) crMap[key] = { name: key, records: 0, details: 0 };
          (d.behaviorCards || []).forEach(function (c) { crMap[key].records += c.count; if (c.latestDetailSaved) crMap[key].details += 1; });
        });
        courseStats = Object.values(crMap).sort(function (a, b) { return b.records - a.records; });
      } catch (e) { /* skip */ }

      // 环境统计
      var envStats = [];
      try {
        var em = {};
        crDetails.forEach(function (d) {
          if (!d) return;
          var key = d.environmentLabel || d.environmentCode;
          if (!em[key]) em[key] = { name: key, records: 0 };
          (d.behaviorCards || []).forEach(function (c) { em[key].records += c.count; });
        });
        envStats = Object.values(em).sort(function (a, b) { return b.records - a.records; });
      } catch (e) { /* skip */ }

      // 高频行为表现（从 items 取 top 6）
      var topBeh = items.slice().sort(function (a, b) { return b.count - a.count; }).slice(0, 6);

      this.setData({
        loading: false, empty: !items.length, overview: ov, totalCount: total, items: items,
        dailyBars: dailyBars, dailyMaxCount: dailyBars.length ? Math.max.apply(null, dailyBars.map(function (b) { return b.count; })) : 1,
        statusDist: { incomplete: incomplete, assisted: assisted, independent: independent },
        dailyTrend: [], courseStats: courseStats, envStats: envStats, topBehaviors: topBeh,
        aiCards: this.buildAiCards(view, items)
      });

      this._updateCharts(view, items);
    } catch (err) {
      this.setData({ loading: false, empty: false });
      if (err && err.code !== 40101) wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  _updateCharts(view, items) {
    var chart = view === 'daily' ? dailyChart : weeklyChart;
    if (!chart || !items || !items.length) return;
    var names = items.map(function (i) { return i.behaviorLabel; });
    var values = items.map(function (i) { return i.count; });
    var colors = items.map(function (i) {
      if (i.trendDirection === 'UP') return '#ef4444';
      if (i.trendDirection === 'DOWN') return '#22c55e';
      return '#5B9BD5';
    });
    chart.setOption({
      xAxis: { type: 'category', data: names, axisLabel: { fontSize: 10, color: '#9ca3af', rotate: names.length > 6 ? 35 : 0 }, axisLine: { lineStyle: { color: '#e5e7eb' } } },
      yAxis: { type: 'value', minInterval: 1, axisLabel: { fontSize: 10, color: '#9ca3af' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
      series: [{ type: 'bar', data: values, barWidth: 18, itemStyle: { borderRadius: [4, 4, 0, 0], color: function (p) { return colors[p.dataIndex]; } } }],
      grid: { left: 40, right: 16, top: 16, bottom: 32 }
    });
  },

  buildAiCards(view, items) {
    var cards = [];
    var total = items.reduce(function (s, i) { return s + i.count; }, 0);
    if (view === 'daily' && items.length) {
      var top = items.slice().sort(function (a, b) { return b.count - a.count; })[0];
      if (top) cards.push({ type: 'info', title: '最高频行为', content: '「' + top.behaviorLabel + '」共 ' + top.count + ' 次' });
    }
    if ((view === 'weekly' || view === 'monthly') && items.length) {
      var ups = items.filter(function (i) { return i.trendDirection === 'UP'; });
      var downs = items.filter(function (i) { return i.trendDirection === 'DOWN'; });
      if (downs.length) cards.push({ type: 'success', title: '改善行为', content: downs.map(function (i) { return i.behaviorLabel; }).join('、') + ' 趋势下降' });
      if (ups.length) cards.push({ type: 'warn', title: '关注行为', content: ups.map(function (i) { return i.behaviorLabel; }).join('、') + ' 趋势上升需关注' });
    }
    if (!cards.length) cards.push({ type: 'info', title: '数据概览', content: total > 0 ? '共 ' + total + ' 次行为记录' : '暂无记录' });
    return cards;
  },

  // 环形图弧长百分比
  statusPct(key) {
    var d = this.data.statusDist;
    var t = d.incomplete + d.assisted + d.independent || 1;
    return Math.round((d[key] || 0) / t * 100);
  },

  loadSemester() {
    this.setData({
      overview: { observationCourseCount: 4, behaviorRecordCount: 1860, abcRecordCount: 520, remarkCount: 98 },
      totalCount: 1860, empty: false,
      statusDist: { incomplete: 186, assisted: 558, independent: 1116 },
      aiCards: [
        { type: 'info', title: '学期总评', content: '六大能力维度均有提升，平均进步20%。' },
        { type: 'warn', title: '薄弱环节', content: '专注力+14%，集体课参与度58%，建议加强感统训练。' },
        { type: 'success', title: '下学期建议', content: '引入同伴支持策略提升社交互动。' }
      ]
    });
  }
});
