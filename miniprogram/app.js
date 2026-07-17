// app.js
App({
  onLaunch() {
    // 登录态判断放在 login 页 onLoad（入口页），onLaunch 里跳转有竞态风险
  },

  globalData: {
    // 白名单身份与后端返回的当前上下文（见 utils/store.js）
    teacherId: '',
    teacher: null,
    currentStudent: null
  }
});
