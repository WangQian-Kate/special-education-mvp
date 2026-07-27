// pages/login/login.js
// 真实微信登录 + 首次注册引导
const { SHADOW_TEACHERS } = require('../../utils/constants');
const store = require('../../utils/store');
const api = require('../../utils/request');

Page({
  data: {
    step: 'splash',
    devTapCount: 0,
    teachers: SHADOW_TEACHERS,
    submitting: false,
    form: {
      teacherName: '', school: '', studentName: '',
      studentAge: '', studentClassName: '', studentRemark: '',
      socialAdaptation: '', selfManagement: '', cognitiveLevel: '',
      languageComprehension: '', expressionAbility: '', hobbies: ''
    },
    roleOptions: ['影子老师', '资源教师', '班主任', '家长'],
    roleIndex: 0,
    genderOptions: ['请选择', '男', '女'],
    genderIndex: 0,
    disabilityChecks: {},
    abilityFields: [
      { key: 'socialAdaptation', label: '社会适应能力' },
      { key: 'selfManagement', label: '自我管理能力' },
      { key: 'cognitiveLevel', label: '认知水平' },
      { key: 'languageComprehension', label: '语言理解' },
      { key: 'expressionAbility', label: '表达能力' }
    ],
  },

  onLoad() {
    // 已有 token 则直接恢复
    var token = store.getAccessToken();
    if (token) {
      this.restoreSession();
      return;
    }
    this.setData({ step: 'login' });
  },

  async restoreSession() {
    try {
      const profile = await api.get('/me', {}, { hideError: true });
      store.setProfile(profile);
      wx.switchTab({ url: '/pages/records/records' });
    } catch (err) {
      store.clearAccessToken();
      this.setData({ step: 'login' });
    }
  },

  async handleWechatLogin() {
    if (this.data.submitting) return;
    this.setData({ submitting: true });
    try {
      var wxRes = await new Promise((resolve, reject) => {
        wx.login({ success: resolve, fail: reject });
      });
      console.log('wx.login code:', wxRes.code ? wxRes.code.substring(0, 10) + '...' : 'EMPTY');
      if (!wxRes.code) throw new Error('wx.login 返回空 code');
      var result = await api.post('/auth/wechat/login', { code: wxRes.code }, { hideError: true });
      this.onLoginSuccess(result);
    } catch (err) {
      this.setData({ submitting: false });
      console.log('login error code:', err && err.code, 'message:', err && err.message);
      var code = err && err.code;
      if (code === 'WECHAT_BINDING_REQUIRED' || code === 40301) {
        this.setData({ step: 'teacher' });
      } else {
        wx.showToast({ title: (err && err.message) || '登录失败请重试', icon: 'none', duration: 3000 });
      }
    }
  },

  onLoginSuccess(result) {
    store.setAccessToken(result.accessToken);
    store.setProfile(result.profile);
    wx.switchTab({ url: '/pages/records/records' });
  },

  onFieldInput(e) {
    var field = e.currentTarget.dataset.field;
    var value = e.detail.value;
    this.setData({ ['form.' + field]: value });
  },

  onRoleChange(e) { this.setData({ roleIndex: Number(e.detail.value) }); },
  onGenderChange(e) { this.setData({ genderIndex: Number(e.detail.value) }); },

  onDisabilityToggle(e) {
    var code = e.currentTarget.dataset.code;
    var checks = this.data.disabilityChecks;
    checks[code] = !checks[code];
    this.setData({ disabilityChecks: checks });
  },

  onAbilityTap(e) {
    var key = e.currentTarget.dataset.key;
    var val = e.currentTarget.dataset.val;
    this.setData({ ['form.' + key]: val });
  },

  onNextStep() {
    if (!this.data.form.teacherName.trim()) { wx.showToast({ title: '请输入姓名', icon: 'none' }); return; }
    if (!this.data.form.school.trim()) { wx.showToast({ title: '请输入学校', icon: 'none' }); return; }
    this.setData({ step: 'student' });
  },

  async handleSubmit() {
    if (this.data.submitting) return;
    if (!this.data.form.studentName.trim()) { wx.showToast({ title: '请输入孩子姓名', icon: 'none' }); return; }
    if (this.data.genderIndex === 0) { wx.showToast({ title: '请选择性别', icon: 'none' }); return; }
    this.setData({ submitting: true });
    try {
      var res = await new Promise((resolve, reject) => {
        wx.login({ success: resolve, fail: reject });
      });
      if (!res.code) throw new Error('wx.login 失败');
      var disabilities = Object.keys(this.data.disabilityChecks).filter(function (k) { return this.data.disabilityChecks[k]; }.bind(this));
      var payload = Object.assign({}, this.data.form, {
        wechatCode: res.code,
        role: this.data.roleOptions[this.data.roleIndex],
        studentGender: this.data.genderOptions[this.data.genderIndex],
        disabilityType: disabilities.join(',') || null,
        studentAge: this.data.form.studentAge || null
      });
      console.log('onboarding payload:', JSON.stringify(payload));
      var result = await api.post('/auth/onboarding', payload, { hideError: true });
      this.onLoginSuccess(result);
    } catch (err) {
      this.setData({ submitting: false });
      wx.showToast({ title: (err && err.message) || '注册失败，请重试', icon: 'none' });
    }
  },

  onDevTap() {
    var n = (this.data.devTapCount || 0) + 1;
    this.setData({ devTapCount: n });
    if (n >= 3) { wx.showToast({ title: '开发模式', icon: 'none' }); this.setData({ step: 'dev', devTapCount: 0 }); }
  },

  async handleDevSelect(e) {
    var teacher = SHADOW_TEACHERS[e.currentTarget.dataset.index];
    store.clearAccessToken();
    store.setTeacherId(teacher.id);
    try {
      var profile = await api.get('/me', {}, { hideError: true, showLoading: true });
      store.setProfile(profile);
      wx.switchTab({ url: '/pages/records/records' });
    } catch (err) {
      store.clearIdentity();
      wx.showToast({ title: '连接失败', icon: 'none' });
    }
  }
});
