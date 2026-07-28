// pages/statistics/statistics.js
// 学生评估页：日报/周报/月报/学期报告
const recordApi = require('../../api/record');
const aiApi = require('../../api/ai');
const aiMock = require('../../mock/ai-report');
const { today, currentSemester } = require('../../utils/datetime');

function pct(a, b) { return b ? Math.round(a / b * 100) : 0; }

/**
 * 将后端 SEAT 格式转换为前端三维度格式
 * 后端返回: { hypothesizedFunction, causalChainAnalysis, antecedentInterventions, replacementBehaviors }
 * 前端期望: { behaviorChanges, attentionConcerns, alternativeSuggestions }
 */
function transformAiReport(report) {
  if (!report) return null;
  // 如果已经是三维度格式（mock 数据），直接返回
  if (report.behaviorChanges) return report;
  // 如果是 SEAT 格式（后端真实数据），进行转换
  if (!report.hypothesizedFunction) return report;

  var hf = report.hypothesizedFunction;
  var chains = report.causalChainAnalysis || [];
  var interventions = report.antecedentInterventions || [];
  var replacements = report.replacementBehaviors || [];

  // 1. behaviorChanges：假设功能 + 因果链
  var behaviorChanges = [];
  if (hf && hf.functionCode !== 'UNKNOWN') {
    behaviorChanges.push({
      title: '行为功能假设：' + (hf.functionLabel || hf.functionCode),
      content: '置信度：' + Math.round((hf.confidence || 0) * 100) + '%（' +
        (hf.confidenceLevel || 'LOW') + '）。' + (hf.reasoning || '')
    });
  } else if (hf) {
    behaviorChanges.push({
      title: '行为功能分析',
      content: hf.reasoning || '当前数据不足，无法进行有效推断。'
    });
  }
  for (var i = 0; i < chains.length; i++) {
    var c = chains[i];
    behaviorChanges.push({
      title: '因果链 ' + (i + 1),
      content: '前因：' + (c.antecedent || '未记录') +
        '\n行为：' + (c.behavior || '未记录') +
        '\n结果：' + (c.consequence || '未记录') +
        '\n维持机制：' + (c.maintainingCycle || '需进一步分析')
    });
  }

  // 2. attentionConcerns：置信度注意事项 + 人工审核提醒
  var attentionConcerns = [];
  if (hf && hf.confidenceLevel === 'LOW') {
    attentionConcerns.push({
      title: '样本量偏低，推断仅供参考',
      content: '当前置信度为 ' + Math.round((hf.confidence || 0) * 100) +
        '%，样本量为 ' + (hf.sampleSize || 0) + ' 条记录。建议继续积累ABC观察记录以提高分析准确性。'
    });
  }
  attentionConcerns.push({
    title: '人工审核提醒',
    content: '本报告由AI基于输入的结构化观察数据自动生成，不包含任何医学诊断，不能替代专业评估。请资源教师、影子老师及相关专业人员在实施干预建议前，结合学生实际日常表现进行人工审核与调整。'
  });

  // 3. alternativeSuggestions：前因干预 + 替代行为
  var alternativeSuggestions = [];
  for (var j = 0; j < interventions.length; j++) {
    var iv = interventions[j];
    alternativeSuggestions.push({
      title: iv.strategy || '干预策略',
      content: (iv.description || '') + '\n依据：' + (iv.rationale || '')
    });
  }
  for (var k = 0; k < replacements.length; k++) {
    var rb = replacements[k];
    alternativeSuggestions.push({
      title: '替代行为：' + (rb.targetBehavior || ''),
      content: '教学策略：' + (rb.teachingStrategy || '') +
        '\n强化计划：' + (rb.reinforcementPlan || '')
    });
  }

  return {
    behaviorChanges: behaviorChanges,
    attentionConcerns: attentionConcerns,
    alternativeSuggestions: alternativeSuggestions
  };
}

Page({
  data: {
    view: 'daily', loading: false, empty: false,
    overview: null, totalCount: 0, items: [],
    // 日报
    dailyBars: [], dailyMaxCount: 1,
    // 周报/月报/学期特有
    statusDist: { incomplete: 0, assisted: 0, independent: 0 },
    // AI 三维度报告
    aiReport: null, aiReportLoading: false,
  },

  onShow() { console.log('[statistics] onShow, view:', this.data.view); this.loadData(this.data.view); },
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
      console.log('[statistics] _loadPeriod enter, view:', view);
      var refDate = today();
      console.log('[statistics] refDate:', refDate);
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
        dailyBars = items.filter(function (i) { return i.count > 0; }).map(function (i) {
          var cnt = i.count || 1;
          var ind = i.independentCount || 0;
          var ass = i.assistedCount || 0;
          var inc = i.incompleteCount || 0;
          return { label: i.behaviorLabel, count: i.count, pct: pct(i.count, total),
                   trend: i.trendDirection,
                   ind: ind, ass: ass, inc: inc,
                   indPct: pct(ind, cnt), assPct: pct(ass, cnt), incPct: pct(inc, cnt) };
        });
      }

      this.setData({
        loading: false, empty: !items.length, overview: ov, totalCount: total, items: items,
        dailyBars: dailyBars, dailyMaxCount: dailyBars.length ? Math.max.apply(null, dailyBars.map(function (b) { return b.count; })) : 1,
        statusDist: { incomplete: incomplete, assisted: assisted, independent: independent }
      });
      console.log('[statistics] view:', view, 'items:', items.length, 'dailyBars:', dailyBars.length, 'ov:', !!ov);

      // 周报/月报：只调 AI 接口（详细图表在随班记录页查看）
      if (view !== 'daily') {
        this._loadAiReport(view === 'weekly' ? 'WEEKLY' : 'MONTHLY', refDate);
      }
    } catch (err) {
      console.error('[statistics] _loadPeriod error:', err);
      this.setData({ loading: false, empty: false });
      if (err && err.code !== 40101) wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  _updateCharts(view, items) {
    if (view === 'daily') return; // 日报已改用纯列表，无图表
    var chart = weeklyChart;
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

  /**
   * 加载 AI 分析报告（优先后端，失败 fallback 到 mock）
   * @param {string} period - 'WEEKLY' | 'MONTHLY' | 'SEMESTER'
   * @param {string} [referenceDate] - 参考日期，用于确保统计和 AI 报告使用相同的日期范围
   */
  async _loadAiReport(period, referenceDate) {
    this.setData({ aiReport: null, aiReportLoading: true });
    try {
      var ds = referenceDate || today();
      console.log('[statistics] _loadAiReport enter, period:', period, 'refDate:', ds);
      var report;
      try {
        report = await aiApi.getAiReport(period, ds);
        console.log('[statistics] AI API response:', report);
        // 将后端 SEAT 格式转换为前端三维度格式
        report = transformAiReport(report);
        console.log('[statistics] AI report transformed:', report);
      } catch (_apiErr) {
        console.warn('[statistics] AI API failed, using mock data:', _apiErr);
        if (period === 'SEMESTER') {
          report = aiMock.SEMESTER;
        } else if (period === 'WEEKLY') {
          report = aiMock.WEEKLY;
        } else {
          report = aiMock.MONTHLY;
        }
      }
      this.setData({ aiReport: report });
    } catch (err) {
      console.error('[statistics] _loadAiReport error:', err);
      this.setData({ aiReport: null });
    } finally {
      this.setData({ aiReportLoading: false });
    }
  },

  async loadSemester() {
    this.setData({ loading: true, empty: false });
    try {
      var sem = currentSemester();
      var refDate = sem.start;
      console.log('[statistics] loadSemester enter, refDate:', refDate);
      var stats = await recordApi.getEvaluationStats('SEMESTER', refDate);
      console.log('[statistics] loadSemester stats:', stats);
      var ov = stats ? stats.overview : null;
      var items = (stats && stats.items) || [];
      var total = stats ? stats.totalCount : 0;

      var incomplete = ov ? (ov.incompleteCount || 0) : 0;
      var assisted = ov ? (ov.assistedCount || 0) : 0;
      var independent = ov ? (ov.independentCount || 0) : 0;

      this.setData({
        loading: false,
        empty: !items.length && (!ov || !ov.behaviorRecordCount),
        overview: ov,
        totalCount: total,
        items: items,
        statusDist: { incomplete: incomplete, assisted: assisted, independent: independent }
      });
      console.log('[statistics] semester loaded, items:', items.length, 'total:', total, 'ov:', ov);

      // 使用学期开始日期作为参考日期，确保与统计数据使用相同的日期范围
      this._loadAiReport('SEMESTER', refDate);
    } catch (err) {
      console.error('[statistics] loadSemester error:', err);
      this.setData({ loading: false, empty: false });
      if (err && err.code !== 40101) wx.showToast({ title: '加载失败', icon: 'none' });
    }
  }
});
