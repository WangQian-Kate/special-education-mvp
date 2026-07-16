// pages/profile/profile.js
// TAB3 我的（MVP 简版：展示当前影子老师 + 切换身份/退出）
const store = require('../../utils/store');

Page({
  data: {
    teacher: null
  },

  onShow() {
    const teacher = store.getTeacher();
    if (!teacher) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.setData({ teacher });
  },

  /** 切换身份 = 清登录态回登录页（伪登录阶段两者等价） */
  handleSwitch() {
    wx.showModal({
      title: '切换身份',
      content: '将返回身份选择页，当前本地未保存数据会丢失',
      success: (res) => {
        if (res.confirm) {
          store.clearTeacher();
          wx.reLaunch({ url: '/pages/login/login' });
        }
      }
    });
  }
});
