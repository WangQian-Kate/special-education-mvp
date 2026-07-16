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
    baseUrl: api.BASE_URL
  },

  onLoad() {
    // 已登录直接进主界面（冷启动兜底，app.js 也有同判断）
    if (store.getTeacher()) {
      wx.switchTab({ url: '/pages/records/records' });
    }
  },

  /** 选择影子老师，进入主界面 */
  handleSelect(e) {
    const teacher = this.data.teachers[e.currentTarget.dataset.index];
    store.setTeacher(teacher);
    wx.switchTab({ url: '/pages/records/records' });
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
