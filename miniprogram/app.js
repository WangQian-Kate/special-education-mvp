// app.js
App({
  onLaunch() {
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync();
    this.globalData.systemInfo = systemInfo;
  },

  globalData: {
    // 用户角色：'teacher' | 'parent' | null
    userRole: null,
    // 系统信息
    systemInfo: null,
    // API 基础地址（本地开发环境通过局域网联调）
    apiBaseUrl: 'http://localhost:3000/api'
  }
});
