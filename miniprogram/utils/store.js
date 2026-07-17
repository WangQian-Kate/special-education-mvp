// utils/store.js
// 轻量全局状态：teacherId 持久化，教师/学生资料由后端实时返回

const KEY_TEACHER_ID = 'teacherId';
const LEGACY_KEY_TEACHER = 'teacher';

/** 获取当前白名单教师 ID，并迁移旧版 teacher 对象缓存 */
function getTeacherId() {
  const app = getApp();
  if (app && app.globalData.teacherId) return app.globalData.teacherId;

  let teacherId = wx.getStorageSync(KEY_TEACHER_ID) || '';
  if (!teacherId) {
    const legacyTeacher = wx.getStorageSync(LEGACY_KEY_TEACHER);
    teacherId = legacyTeacher ? legacyTeacher.teacherId || legacyTeacher.id || '' : '';
    if (teacherId) wx.setStorageSync(KEY_TEACHER_ID, teacherId);
    if (legacyTeacher) wx.removeStorageSync(LEGACY_KEY_TEACHER);
  }

  if (app && teacherId) app.globalData.teacherId = teacherId;
  return teacherId;
}

/** 设置当前白名单教师 ID，切换身份时清空旧资料 */
function setTeacherId(teacherId) {
  const app = getApp();
  if (app) {
    app.globalData.teacherId = teacherId;
    app.globalData.teacher = null;
    app.globalData.currentStudent = null;
  }
  wx.setStorageSync(KEY_TEACHER_ID, teacherId);
  wx.removeStorageSync(LEGACY_KEY_TEACHER);
}

/** 保存后端 /me 返回的当前教师和学生资料 */
function setProfile(profile) {
  const app = getApp();
  if (!app) return;
  app.globalData.teacher = profile && profile.user ? profile.user : null;
  app.globalData.currentStudent = profile && profile.currentStudent ? profile.currentStudent : null;

  if (app.globalData.teacher && app.globalData.teacher.teacherId) {
    app.globalData.teacherId = app.globalData.teacher.teacherId;
    wx.setStorageSync(KEY_TEACHER_ID, app.globalData.teacher.teacherId);
  }
}

/** 获取内存中的后端资料 */
function getProfile() {
  const app = getApp();
  if (!app || !app.globalData.teacher) return null;
  return {
    user: app.globalData.teacher,
    currentStudent: app.globalData.currentStudent
  };
}

/** 清除伪登录身份及当前上下文 */
function clearIdentity() {
  const app = getApp();
  if (app) {
    app.globalData.teacherId = '';
    app.globalData.teacher = null;
    app.globalData.currentStudent = null;
  }
  wx.removeStorageSync(KEY_TEACHER_ID);
  wx.removeStorageSync(LEGACY_KEY_TEACHER);
}

module.exports = { getTeacherId, setTeacherId, getProfile, setProfile, clearIdentity };
