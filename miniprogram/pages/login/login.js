// pages/login/login.js
// 阶段二白名单"伪登录"：点影子老师按钮 → teacherId 入全局状态 → 请求头自动携带
const { SHADOW_TEACHERS } = require('../../utils/constants');
const store = require('../../utils/store');
const api = require('../../utils/request');

Page({
  data: {
    teachers: SHADOW_TEACHERS,
    // 后端连通状态：'' | 'ok' | 'fail'
    pingStatus: '',
    baseUrl: api.BASE_URL,
    isSubmitting: false
  },

  onLoad() {
    if (store.getTeacherId()) this.restoreSession();
  },

  /** 冷启动时使用已缓存 teacherId 向后端恢复当前上下文 */
  async restoreSession() {
    if (this.data.isSubmitting) return;
    this.setData({ isSubmitting: true });
    try {
      const profile = await api.get('/me', {}, { hideError: true, showLoading: true });
      store.setProfile(profile);
      this.setData({ isSubmitting: false });
      wx.switchTab({ url: '/pages/records/records' });
    } catch (err) {
      const unauthorized = err && (err.statusCode === 401 || err.code === 40101);
      if (unauthorized) store.clearIdentity();
      this.setData({ isSubmitting: false });
      wx.showToast({
        title: unauthorized ? '身份已失效，请重新选择' : '无法恢复身份，请检查后端连接',
        icon: 'none'
      });
    }
  },

  /** 选择教师并通过 /me 完成白名单身份验证 */
  async handleSelect(e) {
    if (this.data.isSubmitting) return;
    const teacher = this.data.teachers[e.currentTarget.dataset.index];
    store.setTeacherId(teacher.id);
    this.setData({ isSubmitting: true });
    try {
      const profile = await api.get('/me', {}, { hideError: true, showLoading: true });
      store.setProfile(profile);
      this.setData({ isSubmitting: false });
      wx.switchTab({ url: '/pages/records/records' });
    } catch (err) {
      store.clearIdentity();
      this.setData({ isSubmitting: false });
      wx.showToast({ title: '身份验证失败，请检查后端连接', icon: 'none' });
    }
  },

  /** 前后端基础通信测试（阶段二联调目标） */
  async handlePing() {
    wx.showLoading({ title: '连接中', mask: true });
    try {
      await api.ping();
      this.setData({ pingStatus: 'ok' });
      wx.showToast({ title: '后端连接成功', icon: 'success' });
    } catch (err) {
      this.setData({ pingStatus: 'fail' });
      wx.showToast({ title: '连接失败，检查后端/穿透地址', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
