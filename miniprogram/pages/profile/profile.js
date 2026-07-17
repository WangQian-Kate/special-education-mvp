// pages/profile/profile.js
// TAB4 我的：展示后端返回的当前教师和当前学生
const store = require('../../utils/store');
const api = require('../../utils/request');

Page({
  data: {
    teacher: null,
    currentStudent: null,
    loading: false
  },

  onShow() {
    if (!store.getTeacherId()) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }

    const cached = store.getProfile();
    if (cached) this.applyProfile(cached);
    this.loadProfile();
  },

  applyProfile(profile) {
    this.setData({
      teacher: profile.user || null,
      currentStudent: profile.currentStudent || null
    });
  },

  async loadProfile() {
    if (this.data.loading) return;
    this.setData({ loading: true });
    try {
      const profile = await api.get('/me');
      store.setProfile(profile);
      this.applyProfile(profile);
    } catch (err) {
      if (err && (err.statusCode === 401 || err.code === 40101)) {
        store.clearIdentity();
        wx.reLaunch({ url: '/pages/login/login' });
      }
    } finally {
      this.setData({ loading: false });
    }
  },

  /** 切换身份 = 清登录态回登录页（伪登录阶段两者等价） */
  handleSwitch() {
    wx.showModal({
      title: '切换身份',
      content: '将返回身份选择页，当前本地未保存数据会丢失',
      success: (res) => {
        if (res.confirm) {
          store.clearIdentity();
          wx.reLaunch({ url: '/pages/login/login' });
        }
      }
    });
  }
});
