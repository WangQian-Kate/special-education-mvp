// pages/report/report.js
Page({
  data: {
    // 报告列表
    reports: [],
    // 是否正在生成报告
    generating: false
  },

  onLoad() {
    // TODO: 从后端获取历史报告
  },

  /**
   * 生成新报告（调用 AI 接口）
   */
  handleGenerateReport() {
    this.setData({ generating: true });

    // TODO: 调用后端 AI 接口生成报告
    wx.showLoading({ title: '正在生成报告...' });

    // 模拟生成过程
    setTimeout(() => {
      wx.hideLoading();
      this.setData({ generating: false });
      wx.showToast({ title: '报告生成完成', icon: 'success' });
    }, 2000);
  }
});
