// pages/statistics/statistics.js
// 学生评估页：日报/周报/月报/学期报告
const recordApi = require('../../api/record');
const aiApi = require('../../api/ai');
const aiMock = require('../../mock/ai-report');
const { today } = require('../../utils/datetime');

// ECharts 实例
var dailyChart = null;
var weeklyChart = null;

function initDailyChart(canvas, width, height, dpr) {
  if (!canvas || !width || !height) return null;
  var echarts = require('../../components/ec-canvas/echarts');
  var chart = echarts.init(canvas, null, { width: width, height: height, devicePixelRatio: dpr });
  canvas.setChart(chart); dailyChart = chart; return chart;
}
function initWeeklyChart(canvas, width, height, dpr) {
  if (!canvas || !width || !height) return null;
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
    // AI 三维度报告
    aiReport: null, aiReportLoading: false,
    ecDaily: { onInit: initDailyChart },
    ecWeekly: { onInit: initWeeklyChart }
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
      var refDate = today();
      var stats = await recordApi.getEvaluationStats(period, refDate);
      var items = (stats && stats.items) || [];
      var total = stats ? stats.totalCount : 0;
      var ov = stats ? stats.overview : null;

      // 真实状态计数（后端已提供）
      var incomplete = ov ? (ov.incompleteCount || 0) : 0;
      var assisted = ov ? (ov.assistedCount || 0) : 0;
      var independent = ov ? (ov.independentCount || 0) : 0;

      var dailyBars = [];
      if (view === 'daily') {
        dailyBars = items.map(function (i) {
          return { label: i.behaviorLabel, count: i.count, pct: pct(i.count, total),
                   trend: i.trendDirection,
                   ind: i.independentCount || 0, ass: i.assistedCount || 0, inc: i.incompleteCount || 0 };
        });
      }

      var distTotal = incomplete + assisted + independent || 1;
      this.setData({
        loading: false, empty: !items.length, overview: ov, totalCount: total, items: items,
        dailyBars: dailyBars, dailyMaxCount: dailyBars.length ? Math.max.apply(null, dailyBars.map(function (b) { return b.count; })) : 1,
        statusDist: { incomplete: incomplete, assisted: assisted, independent: independent }
      });

      this._updateCharts(view, items);

      // 周报/月报：只调 AI 接口（详细图表在随班记录页查看）
      if (view !== 'daily') {
        this._loadAiReport(view === 'weekly' ? 'WEEKLY' : 'MONTHLY');
      }
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

  /** 加载 AI 分析报告（优先后端，失败 fallback 到 mock） */
  async _loadAiReport(period) {
    this.setData({ aiReport: null, aiReportLoading: true });
    try {
      var ds = today();
      var report;
      try {
        report = await aiApi.getAiReport(period, ds);
      } catch (_apiErr) {
        report = period === 'WEEKLY' ? aiMock.WEEKLY : aiMock.MONTHLY;
      }
      this.setData({ aiReport: report });
    } catch (err) {
      this.setData({ aiReport: null });
    } finally {
      this.setData({ aiReportLoading: false });
    }
  },

  loadSemester() {
    var d = { incomplete: 186, assisted: 558, independent: 1116 };
    var dt = d.incomplete + d.assisted + d.independent;
    this.setData({
      overview: { observationCourseCount: 4, behaviorRecordCount: 1860, abcRecordCount: 520, remarkCount: 98 },
      totalCount: 1860, empty: false, aiReport: null,
      statusDist: d,
      statusPctIncomplete: Math.round(d.incomplete / dt * 100),
      statusPctAssisted: Math.round(d.assisted / dt * 100),
      statusPctIndependent: Math.round(d.independent / dt * 100)
    });
  }
});
