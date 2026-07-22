// pages/statistics/statistics.js
// 学生评估页：日报/周报/月报/学期报告 + ECharts 图表
const recordApi = require('../../api/record');
const { POSITIVE_BEHAVIOR_CODES } = require('../../utils/constants');
const { today } = require('../../utils/datetime');

// ECharts 实例（全局持有，数据变化时 setOption）
var dailyChart = null;
var weeklyChart = null;

function initDailyChart(canvas, width, height, dpr) {
  var echarts = require('../../components/ec-canvas/echarts');
  var chart = echarts.init(canvas, null, { width: width, height: height, devicePixelRatio: dpr });
  canvas.setChart(chart);
  dailyChart = chart;
  return chart;
}

function initWeeklyChart(canvas, width, height, dpr) {
  var echarts = require('../../components/ec-canvas/echarts');
  var chart = echarts.init(canvas, null, { width: width, height: height, devicePixelRatio: dpr });
  canvas.setChart(chart);
  weeklyChart = chart;
  return chart;
}

Page({
  data: {
    view: 'daily',
    loading: false,
    empty: false,
    overview: null,
    totalCount: 0,
    items: [],
    dailyBars: [],
    dailyMaxCount: 1,
    aiCards: [],
    // ECharts
    ecDaily: { onInit: initDailyChart },
    ecWeekly: { onInit: initWeeklyChart }
  },

  onShow() {
    this.loadData(this.data.view);
  },

  onPullDownRefresh() {
    this.loadData(this.data.view).then(function () { wx.stopPullDownRefresh(); });
  },

  onSwitchView(e) {
    var view = e.currentTarget.dataset.view;
    if (view === this.data.view) return;
    this.setData({ view: view });
    this.loadData(view);
  },

  async loadData(view) {
    if (view === 'semester') { this.loadSemester(); return; }
    var periodMap = { daily: 'DAILY', weekly: 'WEEKLY', monthly: 'MONTHLY' };
    var period = periodMap[view];
    if (!period) return;
    this.setData({ loading: true, empty: false });
    try {
      var stats = await recordApi.getEvaluationStats(period, today());
      var items = (stats && stats.items) ? stats.items : [];
      var totalCount = stats ? stats.totalCount : 0;
      var overview = stats ? stats.overview : null;
      var dailyBars = (view === 'daily') ? items.map(function (it) {
        return { label: it.behaviorLabel, count: it.count, pct: totalCount ? Math.round(it.count / totalCount * 100) : 0, positive: POSITIVE_BEHAVIOR_CODES.includes(it.behaviorCode) };
      }) : [];
      var dailyMaxCount = dailyBars.length ? Math.max.apply(null, dailyBars.map(function (b) { return b.count; })) : 1;

      this.setData({
        loading: false, empty: !items.length,
        overview: overview, totalCount: totalCount, items: items,
        dailyBars: dailyBars, dailyMaxCount: Math.max(dailyMaxCount, 1),
        aiCards: this.buildAiCards(view, items, overview)
      });

      // 更新 ECharts 图表
      this._updateChart(view, items);
    } catch (err) {
      this.setData({ loading: false, empty: false });
      if (err && err.code !== 40101) wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  _updateChart(view, items) {
    var chart = view === 'daily' ? dailyChart : weeklyChart;
    if (!chart || !items || !items.length) return;
    var names = items.map(function (i) { return i.behaviorLabel; });
    var values = items.map(function (i) { return i.count; });
    var colors = items.map(function (i) {
      if (i.trendDirection === 'UP') return '#ef4444';
      if (i.trendDirection === 'DOWN') return '#22c55e';
      return '#5B9BD5';
    });
    var maxVal = Math.max.apply(null, values.concat([1]));
    chart.setOption({
      xAxis: { type: 'category', data: names, axisLabel: { fontSize: 10, color: '#9ca3af', rotate: names.length > 6 ? 30 : 0 }, axisLine: { lineStyle: { color: '#e5e7eb' } } },
      yAxis: { type: 'value', minInterval: 1, max: maxVal < 5 ? 5 : null, axisLabel: { fontSize: 10, color: '#9ca3af' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
      series: [{ type: 'bar', data: values, barWidth: 18, itemStyle: { borderRadius: [4, 4, 0, 0], color: function (p) { return colors[p.dataIndex]; } } }],
      grid: { left: 40, right: 16, top: 16, bottom: 28 }
    });
  },

  buildAiCards(view, items, overview) {
    var cards = [];
    var total = items.reduce(function (s, it) { return s + it.count; }, 0);
    if (view === 'daily' && items.length) {
      var top = items.slice().sort(function (a, b) { return b.count - a.count; })[0];
      cards.push({ type: 'info', title: '最高频行为', content: '「' + top.behaviorLabel + '」共 ' + top.count + ' 次，占 ' + Math.round(top.count / total * 100) + '%' });
      if (overview && overview.behaviorRecordCount) {
        cards.push({ type: 'success', title: '今日概况', content: overview.behaviorRecordCount + ' 次行为记录，' + overview.observationCourseCount + ' 节课程，' + overview.abcRecordCount + ' 条详录' });
      }
    }
    if ((view === 'weekly' || view === 'monthly') && items.length) {
      var ups = items.filter(function (i) { return i.trendDirection === 'UP'; });
      var downs = items.filter(function (i) { return i.trendDirection === 'DOWN'; });
      if (downs.length) cards.push({ type: 'success', title: '下降行为', content: downs.map(function (i) { return i.behaviorLabel; }).join('、') + ' 减少，干预有效' });
      if (ups.length) cards.push({ type: 'warn', title: '上升行为', content: ups.map(function (i) { return i.behaviorLabel; }).join('、') + ' 增加，需关注' });
      if (!ups.length && !downs.length) cards.push({ type: 'info', title: '趋势平稳', content: '各行为较上周期无明显变化' });
    }
    if (!cards.length) cards.push({ type: 'info', title: '数据概览', content: total > 0 ? '当前周期共 ' + total + ' 次行为记录' : '暂无记录' });
    return cards;
  },

  loadSemester() {
    var overview = { observationCourseCount: 4, behaviorRecordCount: 1860, abcRecordCount: 520, remarkCount: 98 };
    this.setData({
      overview: overview, totalCount: 1860, empty: false,
      aiCards: [
        { type: 'info', title: '学期总评', content: '本学期六大能力维度均有提升，平均进步20%，干预策略整体有效。' },
        { type: 'warn', title: '薄弱环节', content: '专注力维度进步最小（+14%），集体课参与度58%，建议加强。' },
        { type: 'success', title: '下学期建议', content: '增加感统训练改善专注力；引入同伴支持提升社交互动。' }
      ]
    });
  }
});
