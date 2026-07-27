// utils/store.js
// 轻量全局状态：accessToken + teacherId 持久化，教师/学生资料由后端实时返回

const KEY_ACCESS_TOKEN = 'accessToken';
const KEY_TEACHER_ID = 'teacherId';
const LEGACY_KEY_TEACHER = 'teacher';

function getAccessToken() {
  var app = getApp();
  if (app && app.globalData.accessToken) return app.globalData.accessToken;
  var token = wx.getStorageSync(KEY_ACCESS_TOKEN) || '';
  if (app && token) app.globalData.accessToken = token;
  return token;
}

function setAccessToken(token) {
  var app = getApp();
  if (app) app.globalData.accessToken = token;
  wx.setStorageSync(KEY_ACCESS_TOKEN, token);
}

function clearAccessToken() {
  var app = getApp();
  if (app) app.globalData.accessToken = '';
  wx.removeStorageSync(KEY_ACCESS_TOKEN);
}

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

var ROLE_MAP = { 'SHADOW_TEACHER': '影子老师', 'RESOURCE_TEACHER': '资源教师', 'PARENT': '家长' };
var DISABILITY_MAP = { 'ASD': '孤独症谱系障碍', 'ID': '智力障碍', 'ADHD': '注意缺陷多动障碍', 'SLD': '言语语言障碍', 'LD': '学习障碍', 'DD': '发育迟缓', 'EBD': '情绪行为障碍', 'CP': '脑瘫' };

/** 保存后端 /me 返回的当前教师和学生资料 */
function setProfile(profile) {
  var app = getApp();
  if (!app) return;
  if (profile && profile.user) {
    profile.user.roleLabel = profile.user.position || ROLE_MAP[profile.user.role] || profile.user.role;
  }
  if (profile && profile.currentStudent && profile.currentStudent.disabilityType) {
    profile.currentStudent.disabilityLabel = profile.currentStudent.disabilityType.split(',').map(function (c) { return DISABILITY_MAP[c.trim()] || c.trim(); }).join('、');
  }
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

module.exports = { getAccessToken, setAccessToken, clearAccessToken, getTeacherId, setTeacherId, getProfile, setProfile, clearIdentity };
