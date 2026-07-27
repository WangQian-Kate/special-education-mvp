const store = require('../../utils/store');
const api = require('../../utils/request');

Page({
  data: {
    teacher: null, currentStudent: null, loading: false, saving: false,
    detailVisible: false, switchVisible: false, addVisible: false, profileVisible: false,
    editingStudent: {}, editingProfile: {}, newStudent: {}, studentList: [],
    genderOpts: ['请选择', '男', '女'],
    abilityFields: [
      { key: 'socialAdaptation', label: '社会适应能力' },
      { key: 'selfManagement', label: '自我管理能力' },
      { key: 'cognitiveLevel', label: '认知水平' },
      { key: 'languageComprehension', label: '语言理解' },
      { key: 'expressionAbility', label: '表达能力' }
    ],
    disabilityOptions: [
      { code: 'ASD', label: '孤独症谱系障碍（ASD）' },
      { code: 'ID', label: '智力障碍' },
      { code: 'ADHD', label: '注意缺陷多动障碍（ADHD）' },
      { code: 'SLD', label: '言语语言障碍' },
      { code: 'LD', label: '学习障碍' },
      { code: 'DD', label: '发育迟缓' },
      { code: 'EBD', label: '情绪行为障碍' },
      { code: 'CP', label: '脑瘫' }
    ],
    detailDisabilityChecks: {}, newDisabilityChecks: {},
    roleOptions: ['影子老师', '资源教师', '班主任', '家长'],
    ROLE_ENUM: ['SHADOW_TEACHER', 'RESOURCE_TEACHER', 'RESOURCE_TEACHER', 'PARENT'],
  },

  onShow() {
    if (!store.getAccessToken() && !store.getTeacherId()) { wx.reLaunch({ url: '/pages/login/login' }); return; }
    var cached = store.getProfile();
    if (cached) this.applyProfile(cached);
    this.loadProfile();
  },

  applyProfile(profile) {
    if (!profile) return;
    this.setData({ teacher: profile.user || null, currentStudent: profile.currentStudent || null });
  },

  async loadProfile() {
    if (this.data.loading) return;
    this.setData({ loading: true });
    try {
      var profile = await api.get('/me'); store.setProfile(profile); this.applyProfile(profile);
    } catch (err) {
      if (err && (err.statusCode === 401 || err.code === 40101)) { store.clearIdentity(); store.clearAccessToken(); wx.reLaunch({ url: '/pages/login/login' }); }
    } finally { this.setData({ loading: false }); }
  },

  // ===== 学生详情弹窗 =====
  async onStudentTap() {
    var sid = this.data.currentStudent && this.data.currentStudent.id;
    if (!sid) return;
    try {
      var detail = await api.get('/me/students/' + sid);
      detail._genderIdx = detail.gender === 'MALE' ? 1 : detail.gender === 'FEMALE' ? 2 : 0;
      var dch = {};
      (detail.disabilityType || '').split(',').forEach(function (c) { var t = c.trim(); if (t) dch[t] = true; });
      this.setData({ detailVisible: true, editingStudent: detail, detailDisabilityChecks: dch });
    } catch (err) { wx.showToast({ title: '加载失败', icon: 'none' }); }
  },
  onDetailClose() { this.setData({ detailVisible: false }); },
  onEditField(e) {
    var f = e.currentTarget.dataset.field;
    this.setData({ ['editingStudent.' + f]: e.detail.value });
  },
  onEditGender(e) {
    var idx = Number(e.detail.value);
    var g = idx === 1 ? 'MALE' : idx === 2 ? 'FEMALE' : '';
    this.setData({ 'editingStudent._genderIdx': idx, 'editingStudent.gender': g });
  },
  onEditAbility(e) {
    var key = e.currentTarget.dataset.key;
    var val = e.currentTarget.dataset.val;
    this.setData({ ['editingStudent.' + key]: val });
  },
  async onSaveStudent() {
    this.setData({ saving: true });
    try {
      var s = this.data.editingStudent;
      var result = await api.put('/me/students/' + s.id, {
        name: s.name, gender: s.gender, age: s.age ? parseInt(s.age) : null,
        className: s.className,
        disabilityType: this._buildDisability(this.data.detailDisabilityChecks),
        remark: s.remark, socialAdaptation: s.socialAdaptation, selfManagement: s.selfManagement,
        cognitiveLevel: s.cognitiveLevel, languageComprehension: s.languageComprehension,
        expressionAbility: s.expressionAbility, hobbies: s.hobbies
      });
      store.setProfile({ user: this.data.teacher, currentStudent: result });
      this.applyProfile({ user: this.data.teacher, currentStudent: result });
      wx.showToast({ title: '已保存', icon: 'success' }); this.setData({ detailVisible: false });
    } catch (err) { wx.showToast({ title: '保存失败', icon: 'none' }); }
    this.setData({ saving: false });
  },

  // ===== 切换学生 =====
  async onSwitchStudentTap() {
    try {
      var list = await api.get('/me/students');
      this.setData({ switchVisible: true, studentList: list });
    } catch (err) { wx.showToast({ title: '加载失败', icon: 'none' }); }
  },
  onSwitchClose() { this.setData({ switchVisible: false }); },
  async onSelectStudent(e) {
    var sid = Number(e.currentTarget.dataset.id);
    try {
      var result = await api.put('/me/current-student', { studentId: sid });
      store.setProfile({ user: this.data.teacher, currentStudent: result });
      this.applyProfile({ user: this.data.teacher, currentStudent: result });
      this.setData({ switchVisible: false });
    } catch (err) { wx.showToast({ title: '切换失败', icon: 'none' }); }
  },

  // ===== 新增学生 =====
  onAddStudentTap() {
    this.setData({ switchVisible: false, addVisible: true, newStudent: { name: '', age: '', _genderIdx: 0, className: '', disabilityType: '', hobbies: '', remark: '' } });
  },
  onAddClose() { this.setData({ addVisible: false }); },
  onNewField(e) { var f = e.currentTarget.dataset.field; this.setData({ ['newStudent.' + f]: e.detail.value }); },
  onNewGender(e) {
    var idx = Number(e.detail.value);
    this.setData({ 'newStudent._genderIdx': idx, 'newStudent.gender': idx === 1 ? 'MALE' : idx === 2 ? 'FEMALE' : '' });
  },
  onNewAbility(e) { var key = e.currentTarget.dataset.key; this.setData({ ['newStudent.' + key]: e.currentTarget.dataset.val }); },
  async onCreateStudent() {
    var s = this.data.newStudent;
    if (!s.name) { wx.showToast({ title: '请输入姓名', icon: 'none' }); return; }
    this.setData({ saving: true });
    try {
      var result = await api.post('/me/students', {
        name: s.name, gender: s.gender, age: s.age ? parseInt(s.age) : null,
        className: s.className, disabilityType: this._buildDisability(this.data.newDisabilityChecks),
        remark: s.remark, socialAdaptation: s.socialAdaptation, selfManagement: s.selfManagement,
        cognitiveLevel: s.cognitiveLevel, languageComprehension: s.languageComprehension,
        expressionAbility: s.expressionAbility, hobbies: s.hobbies
      });
      store.setProfile({ user: this.data.teacher, currentStudent: result });
      this.applyProfile({ user: this.data.teacher, currentStudent: result });
      wx.showToast({ title: '已创建', icon: 'success' }); this.setData({ addVisible: false });
    } catch (err) { wx.showToast({ title: '创建失败', icon: 'none' }); }
    this.setData({ saving: false });
  },

  // ===== 修改个人资料 =====
  onEditProfileTap() {
    var t = this.data.teacher || {};
    var roleIdx = this.data.roleOptions.indexOf(t.position) || this.data.ROLE_ENUM.indexOf(t.role);
    if (roleIdx < 0) roleIdx = 1;
    this.setData({ profileVisible: true, editingProfile: { name: t.name || '', school: t.school || '', _roleIdx: roleIdx } });
  },
  onEditRole(e) { this.setData({ 'editingProfile._roleIdx': Number(e.detail.value) }); },
  onProfileClose() { this.setData({ profileVisible: false }); },
  onProfileField(e) { var f = e.currentTarget.dataset.field; this.setData({ ['editingProfile.' + f]: e.detail.value }); },
  async onSaveProfile() {
    this.setData({ saving: true });
    try {
      var p = this.data.editingProfile;
      var payload = { name: p.name, school: p.school, role: this.data.ROLE_ENUM[p._roleIdx], position: this.data.roleOptions[p._roleIdx] };
      await api.put('/me/profile', payload);
      var cached = store.getProfile();
      if (cached && cached.user) {
        cached.user.name = p.name; cached.user.school = p.school;
        cached.user.role = payload.role; cached.user.roleLabel = this.data.roleOptions[p._roleIdx];
        cached.user.position = payload.position;
        store.setProfile(cached);
      }
      this.applyProfile(cached);
      wx.showToast({ title: '已保存', icon: 'success' }); this.setData({ profileVisible: false });
    } catch (err) { wx.showToast({ title: '保存失败', icon: 'none' }); }
    this.setData({ saving: false });
  },

  onDetailDisabilityToggle(e) {
    var c = e.currentTarget.dataset.code; var ch = this.data.detailDisabilityChecks;
    ch[c] = !ch[c]; this.setData({ detailDisabilityChecks: ch });
  },
  onNewDisabilityToggle(e) {
    var c = e.currentTarget.dataset.code; var ch = this.data.newDisabilityChecks;
    ch[c] = !ch[c]; this.setData({ newDisabilityChecks: ch });
  },
  _buildDisability(checks) {
    var codes = Object.keys(checks).filter(function (k) { return checks[k]; });
    return codes.join(',') || null;
  },
  noop() {},

  handleLogout() {
    wx.showModal({
      title: '退出登录', content: '将返回登录页',
      success: function (res) { if (res.confirm) { store.clearIdentity(); store.clearAccessToken(); wx.reLaunch({ url: '/pages/login/login' }); } }
    });
  }
});
