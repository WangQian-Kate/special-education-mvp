// pages/index/index.js
Page({
  data: {
    // 用户选择的角色：'' | 'teacher' | 'parent'
    selectedRole: ''
  },

  onLoad() {
    // 页面加载
  },

  /**
   * 选择角色
   */
  handleRoleSelect(e) {
    const role = e.currentTarget.dataset.role;
    this.setData({ selectedRole: role });
    const app = getApp();
    app.globalData.userRole = role;

    wx.showToast({
      title: role === 'teacher' ? '已选择：教师' : '已选择：家长',
      icon: 'success'
    });
  }
});
