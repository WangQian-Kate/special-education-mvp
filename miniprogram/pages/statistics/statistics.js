// pages/statistics/statistics.js
// 学生评估页：日报/周报/月报/学期报告四视图（3.2：接入 EVAL-001 真实数据）
const recordApi = require('../../api/record');
const { POSITIVE_BEHAVIOR_CODES } = require('../../utils/constants');
const { today } = require('../../utils/datetime');

Page({
  data: {
    view: 'daily',
    loading: false,
    empty: false,
    // 统计数据（EVAL-001 返回）
    overview: null,     // { totalCourses?, totalRecords?, totalAbcRecords?, totalNotes? }
    totalCount: 0,
    items: [],          // [{ behaviorCode, behaviorLabel, count, changePercent, trendDirection }]
    // 日报柱状图（CSS bars）
    dailyBars: [],
    dailyMaxCount: 1,
    // AI 卡片（静态 mock，阶段四换大模型接口）
    aiCards: []
  },

  onShow() {
    this.loadData(this.data.view);
  },

  onPullDownRefresh() {
    this.loadData(this.data.view).then(() => wx.stopPullDownRefresh());
  },

  onSwitchView(e) {
    const view = e.currentTarget.dataset.view;
    if (view === this.data.view) return;
    this.setData({ view });
    this.loadData(view);
  },

  async loadData(view) {
    if (view === 'semester') {
      this.loadSemester();
      return;
    }
    const periodMap = { daily: 'DAILY', weekly: 'WEEKLY', monthly: 'MONTHLY' };
    const period = periodMap[view];
    if (!period) return;
    this.setData({ loading: true, empty: false });
    try {
      const stats = await recordApi.getEvaluationStats(period, today());
      const items = (stats && stats.items) ? stats.items : [];
      const totalCount = stats ? stats.totalCount : 0;
      const overview = stats ? stats.overview : null;
      const dailyBars = view === 'daily'
        ? items.map((it) => ({ label: it.behaviorLabel, count: it.count, positive: POSITIVE_BEHAVIOR_CODES.includes(it.behaviorCode) }))
        : [];
      const dailyMaxCount = dailyBars.length ? Math.max(...dailyBars.map((b) => b.count), 1) : 1;
      this.setData({
        loading: false, empty: !items.length,
        overview, totalCount, items, dailyBars, dailyMaxCount,
        aiCards: this.buildAiCards(view, items)
      });
    } catch (err) {
      this.setData({ loading: false, empty: false });
      // 40101 已在 request.js 统一处理
      if (err && err.code !== 40101) wx.showToast({ title: '加载统计数据失败', icon: 'none' });
    }
  },

  buildAiCards(view, items) {
    const cards = [];
    const total = items.reduce((s, it) => s + it.count, 0);
    if (view === 'daily' && items.length) {
      const top = [...items].sort((a, b) => b.count - a.count)[0];
      cards.push({ type: 'info', title: '今日最高频', content: `「${top.behaviorLabel}」共发生 ${top.count} 次，占比 ${((top.count / total) * 100).toFixed(0)}%` });
    }
    if (view === 'weekly' && items.length) {
      const upItems = items.filter((it) => it.trendDirection === 'UP');
      const downItems = items.filter((it) => it.trendDirection === 'DOWN');
      if (upItems.length) cards.push({ type: 'info', title: '上升行为', content: `${upItems.map((i) => i.behaviorLabel).join('、')} 较上周增加，需关注` });
      if (downItems.length) cards.push({ type: 'success', title: '下降行为', content: `${downItems.map((i) => i.behaviorLabel).join('、')} 较上周减少，干预有效` });
    }
    if (view === 'monthly' && items.length) {
      cards.push({ type: 'info', title: '月度总评', content: `本月共记录 ${total} 次行为，覆盖 ${items.length} 种行为类型` });
    }
    if (!cards.length) cards.push({ type: 'info', title: '数据概览', content: total > 0 ? `当前周期共记录 ${total} 次行为` : '暂无行为记录，开始记录后将自动生成分析' });
    return cards;
  },

  loadSemester() {
    // 学期报告后端暂无接口，保留静态占位
    const overview = { observationCourseCount: 4, behaviorRecordCount: 1860, abcRecordCount: 520, remarkCount: 98 };
    this.setData({ overview, totalCount: 1860, empty: false,
      aiCards: [
        { type: 'info', title: '学期总评', content: '本学期六大能力维度均有显著提升，平均进步20%，干预策略整体有效。' },
        { type: 'warn', title: '薄弱环节', content: '专注力维度进步最小（+14%），集体课参与度仅58%，建议下学期重点加强。' },
        { type: 'success', title: '下学期建议', content: '增加感统训练频次改善专注力；引入同伴支持策略提升社交互动。' }
      ]
    });
  },

  /** 趋势方向 → 颜色 */
  trendColor(dir) {
    return dir === 'UP' ? '#ef4444' : dir === 'DOWN' ? '#22c55e' : '#9ca3af';
  },

  trendArrow(dir) {
    return dir === 'UP' ? '↑' : dir === 'DOWN' ? '↓' : '→';
  },

  /** 环比变化百分比文案 */
  changeText(item) {
    if (item.changePercent == null) return '新增';
    const sign = item.changePercent >= 0 ? '+' : '';
    return `${sign}${item.changePercent}%`;
  }
});
