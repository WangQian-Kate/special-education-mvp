// app.js
App({
  onLaunch() {
    // 登录态判断放在 login 页 onLoad（入口页），onLaunch 里跳转有竞态风险
  },

  globalData: {
    // 当前教师（伪登录写入，见 utils/store.js）
    teacher: null
  }
});
