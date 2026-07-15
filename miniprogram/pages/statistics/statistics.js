// pages/statistics/statistics.js
Page({
  data: {
    // 统计周期
    dateRange: 'week',  // 'week' | 'month' | 'semester'
    // 统计数据（示例）
    summary: {
      totalRecords: 0,
      behaviorTypes: [],
      intensityAvg: 0
    }
  },

  onLoad() {
    // TODO: 从后端获取统计数据
  },

  /**
   * 切换统计周期
   */
  handleDateRangeChange(e) {
    const { range } = e.currentTarget.dataset;
    this.setData({ dateRange: range });
    // TODO: 重新获取数据
  }
});
