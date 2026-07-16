// utils/store.js
// 轻量全局状态：globalData + storage 双写，避免引入状态库

const KEY_TEACHER = 'teacher';

/** 获取当前教师（优先内存，其次 storage） */
function getTeacher() {
  const app = getApp();
  if (app && app.globalData.teacher) return app.globalData.teacher;
  const cached = wx.getStorageSync(KEY_TEACHER) || null;
  if (app && cached) app.globalData.teacher = cached;
  return cached;
}

/** 设置当前教师（伪登录/切换角色时调用） */
function setTeacher(teacher) {
  const app = getApp();
  if (app) app.globalData.teacher = teacher;
  wx.setStorageSync(KEY_TEACHER, teacher);
}

/** 清除登录态 */
function clearTeacher() {
  const app = getApp();
  if (app) app.globalData.teacher = null;
  wx.removeStorageSync(KEY_TEACHER);
}

module.exports = { getTeacher, setTeacher, clearTeacher };
