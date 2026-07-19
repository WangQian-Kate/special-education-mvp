// pages/records/records.js
// TAB1 随班记录：日/周/月三视图 + 全天汇总特殊模式
const recordApi = require('../../api/record');
const {
  COURSES, ALL_DAY_COURSE, ENVIRONMENTS, COURSE_DEFAULT_ENV,
  DURATION_PRESETS, DEFAULT_DURATION, DURATION_MAX, POSITIVE_BEHAVIOR_CODES
} = require('../../utils/constants');
const { today } = require('../../utils/datetime');
const store = require('../../utils/store');

const NOTE_DEBOUNCE_MS = 800;

function idxByCode(list, code) { const i = list.findIndex((it) => it.code === code); return i >= 0 ? i : 0; }

function mapCards(cards) {
  return (cards || []).map((c) => ({
    behaviorCode: c.behaviorCode, behaviorLabel: c.behaviorLabel,
    count: c.count, latestRecordId: c.latestRecordId, latestDetailSaved: c.latestDetailSaved,
    positive: POSITIVE_BEHAVIOR_CODES.includes(c.behaviorCode), pending: false
  }));
}

function mapOptions(options) {
  return (options || []).map((o) => ({
    behaviorCode: o.code, behaviorLabel: o.label,
    count: 0, latestRecordId: null, latestDetailSaved: null,
    positive: POSITIVE_BEHAVIOR_CODES.includes(o.code), pending: false
  }));
}

Page({
  data: {
    date: '', todayStr: '',
    courses: COURSES, environments: ENVIRONMENTS, durationPresets: DURATION_PRESETS,
    courseIndex: 0, envIndex: 0, durationMinutes: DEFAULT_DURATION._default,
    classRecordId: null, behaviors: [], note: '', noteSaveState: '',
    loading: false,
    recordView: 'day',           // 'day'|'week'|'month'
    weekData: null, monthData: null,  // CLASS-004 summary
    evalFields: [
      { key: 'emotion', label: '情绪行为' },
      { key: 'adaptation', label: '社会适应能力' },
      { key: 'social', label: '社会交往能力' },
      { key: 'selfMgmt', label: '自我行为管理能力' },
      { key: 'language', label: '语言理解及表达' },
      { key: 'focus', label: '自身/共同专注力' }
    ],
    evaluation: { emotion: '', adaptation: '', social: '', selfMgmt: '', language: '', focus: '' },
    dayStats: [],   // 全天汇总行为统计（EVAL-001?period=DAILY）
    abcVisible: false, abcBehavior: null
  },

  onLoad() {
    const t = today();
    this.dayRecords = []; this._epoch = 0; this._noteTimer = null; this._noteSaving = false; this._createPromise = null;
    this.setData({ date: t, todayStr: t });
    this.loadDay(t);
  },

  onShow() { if (!store.getTeacherId()) wx.reLaunch({ url: '/pages/login/login' }); },
  onHide() { this._flushNote(); },
  onUnload() { this._flushNote(); },

  isAllDay() { return COURSES[this.data.courseIndex].code === ALL_DAY_COURSE; },

  // ==================== 视图切换 ====================

  switchRecordView(e) {
    const view = e.currentTarget.dataset.view;
    if (view === this.data.recordView) return;
    if (this.data.recordView === 'day') this._flushNote();
    this.setData({ recordView: view });
    if (view === 'week') this.loadWeek();
    else if (view === 'month') this.loadMonth();
  },

  // ==================== 日视图加载 ====================

  async loadDay(date) {
    this._epoch += 1; const epoch = this._epoch; this._createPromise = null;
    this.setData({ loading: true });
    try {
      const list = await recordApi.getDayRecords(date);
      if (epoch !== this._epoch) return;
      this.dayRecords = list || [];
      if (this.dayRecords.length) await this.bindRecord(this.dayRecords[this.dayRecords.length - 1].id, epoch);
      else await this.resetToEmpty(COURSES[this.data.courseIndex].code, epoch);
    } catch (err) {
      if (epoch !== this._epoch) return;
      this.dayRecords = [];
      this.setData({ classRecordId: null, behaviors: [], note: '', noteSaveState: '' });
      if (err && err.code === 40002) wx.showToast({ title: '请先在「我的」页选择当前学生', icon: 'none' });
    } finally { if (epoch === this._epoch) this.setData({ loading: false }); }
  },

  async bindRecord(id, epoch) {
    const detail = await recordApi.getClassRecordDetail(id);
    if (epoch !== this._epoch) return;
    this.setData({
      classRecordId: detail.id,
      courseIndex: idxByCode(COURSES, detail.courseCode),
      envIndex: idxByCode(ENVIRONMENTS, detail.environmentCode),
      durationMinutes: detail.observationDurationMinutes,
      note: detail.overallRemark || '', noteSaveState: '',
      behaviors: mapCards(detail.behaviorCards)
    });
  },

  /** EMPTY 态：全天汇总不拉卡片；自动设默认环境 */
  async resetToEmpty(courseCode, epoch) {
    const allDay = courseCode === ALL_DAY_COURSE;
    const envCode = COURSE_DEFAULT_ENV[courseCode] || 'CLASSROOM';
    this.setData({
      classRecordId: null,
      courseIndex: idxByCode(COURSES, courseCode),
      envIndex: idxByCode(ENVIRONMENTS, envCode),
      durationMinutes: allDay ? 480 : (DEFAULT_DURATION[courseCode] || DEFAULT_DURATION._default),
      note: '', noteSaveState: '', behaviors: []
    });
    if (allDay) return;
    try { const opts = await recordApi.getBehaviorOptions(courseCode); if (epoch === this._epoch) this.setData({ behaviors: mapOptions(opts) }); } catch (err) {}
  },

  async refreshCards() {
    if (!this.data.classRecordId) return;
    const epoch = this._epoch;
    try {
      const detail = await recordApi.getClassRecordDetail(this.data.classRecordId);
      if (epoch === this._epoch) this.setData({ behaviors: mapCards(detail.behaviorCards) });
    } catch (err) {}
  },

  ensureClassRecord(extra = {}) {
    if (this.isAllDay()) return Promise.reject(new Error('全天汇总'));
    if (this.data.classRecordId) return Promise.resolve(this.data.classRecordId);
    if (this._createPromise) return this._createPromise;
    const epoch = this._epoch;
    const payload = {
      recordDate: this.data.date,
      courseCode: COURSES[this.data.courseIndex].code,
      environmentCode: ENVIRONMENTS[this.data.envIndex].code,
      observationDurationMinutes: this.data.durationMinutes, ...extra
    };
    this._createPromise = recordApi.createClassRecord(payload).then((detail) => {
      this._createPromise = null;
      if (epoch !== this._epoch) return detail.id;
      this.dayRecords.push(detail);
      this.setData({ classRecordId: detail.id, behaviors: mapCards(detail.behaviorCards) });
      return detail.id;
    }).catch((err) => { this._createPromise = null; throw err; });
    return this._createPromise;
  },

  // ==================== 选择器 ====================

  async onDateChange(e) {
    const date = e.detail.value;
    if (date === this.data.date) return;
    await this._flushNote(); this.setData({ date }); this.loadDay(date);
  },

  async onCourseChange(e) {
    const ci = Number(e.detail.value);
    if (ci === this.data.courseIndex) return;
    const code = COURSES[ci].code;
    await this._flushNote();
    this._epoch += 1; const epoch = this._epoch; this._createPromise = null;
    this.setData({ courseIndex: ci });
    // 全天汇总 → 直接转 EMPTY + 拉行为统计
    if (code === ALL_DAY_COURSE) { await this.resetToEmpty(code, epoch); this.loadDayStats(); return; }
    const matches = this.dayRecords.filter((r) => r.courseCode === code);
    try {
      if (matches.length) await this.bindRecord(matches[matches.length - 1].id, epoch);
      else await this.resetToEmpty(code, epoch);
    } catch (err) { if (epoch === this._epoch) wx.showToast({ title: '加载课程记录失败', icon: 'none' }); }
  },

  onEnvChange(e) {
    if (this.isAllDay()) return;
    const ei = Number(e.detail.value); const prev = this.data.envIndex;
    if (ei === prev) return;
    this.setData({ envIndex: ei });
    if (!this.data.classRecordId) return;
    recordApi.patchClassRecord(this.data.classRecordId, { environmentCode: ENVIRONMENTS[ei].code })
      .catch(() => { this.setData({ envIndex: prev }); wx.showToast({ title: '环境保存失败', icon: 'none' }); });
  },

  onDurationChange(e) {
    if (this.isAllDay()) return;
    const preset = DURATION_PRESETS[Number(e.detail.value)];
    if (preset.value === 'custom') {
      wx.showModal({ title: '自定义观察周期', editable: true, placeholderText: `输入分钟数（1-${DURATION_MAX}）`,
        success: (res) => {
          if (!res.confirm) return;
          const v = parseInt(String(res.content || '').trim(), 10);
          if (!v || v < 1 || v > DURATION_MAX || String(v) !== String(res.content || '').trim()) { wx.showToast({ title: `请输入 1-${DURATION_MAX} 的整数`, icon: 'none' }); return; }
          this._applyDuration(v);
        }
      });
    } else this._applyDuration(preset.value);
  },

  async _applyDuration(min) {
    const prev = this.data.durationMinutes; if (min === prev) return;
    this.setData({ durationMinutes: min });
    if (!this.data.classRecordId) return;
    recordApi.patchClassRecord(this.data.classRecordId, { observationDurationMinutes: min })
      .catch(() => { this.setData({ durationMinutes: prev }); wx.showToast({ title: '周期保存失败', icon: 'none' }); });
  },

  // ==================== 备注 ====================

  onNoteInput(e) { this.setData({ note: e.detail.value }); if (this._noteTimer) clearTimeout(this._noteTimer); this._noteTimer = setTimeout(() => { this._noteTimer = null; this._saveNote(); }, NOTE_DEBOUNCE_MS); },
  onNoteRetryTap() { if (this.data.noteSaveState === 'error') this._saveNote(); },

  _flushNote() { if (this._noteTimer) { clearTimeout(this._noteTimer); this._noteTimer = null; return this._saveNote(); } return Promise.resolve(); },

  async _saveNote(isRetry) {
    if (this._noteSaving || this.isAllDay()) return;
    const value = this.data.note;
    if (!this.data.classRecordId && !value.trim()) return;
    this._noteSaving = true; const epoch = this._epoch;
    this.setData({ noteSaveState: 'saving' });
    try {
      if (this.data.classRecordId) await recordApi.patchClassRecord(this.data.classRecordId, { overallRemark: value || null });
      else await this.ensureClassRecord({ overallRemark: value });
      this._noteSaving = false; if (epoch !== this._epoch) return;
      this.setData({ noteSaveState: 'saved' });
      if (this.data.note !== value) this._saveNote();
    } catch (err) {
      this._noteSaving = false; if (epoch !== this._epoch) return;
      if (!isRetry) setTimeout(() => { if (epoch === this._epoch) this._saveNote(true); }, 2000);
      else this.setData({ noteSaveState: 'error' });
    }
  },

  // ==================== 行为计数 ====================

  onCountChange(e) {
    if (this.isAllDay()) return;
    const { behaviorCode, delta } = e.detail;
    delta > 0 ? this._handlePlus(behaviorCode) : this._handleMinus(behaviorCode);
  },

  _cardIdx(code) { return this.data.behaviors.findIndex((b) => b.behaviorCode === code); },
  _patchCard(i, p) { this.setData({ [`behaviors[${i}]`]: { ...this.data.behaviors[i], ...p } }); },

  async _handlePlus(code) {
    const idx = this._cardIdx(code); if (idx < 0 || this.data.behaviors[idx].pending) return;
    const epoch = this._epoch;
    this._patchCard(idx, { pending: true, count: this.data.behaviors[idx].count + 1 });
    try {
      const recordId = await this.ensureClassRecord(); if (epoch !== this._epoch) return;
      const result = await recordApi.quickAddBehavior(recordId, code); if (epoch !== this._epoch) return;
      const i2 = this._cardIdx(code); if (i2 >= 0) this._patchCard(i2, mapCards([result.card])[0]);
    } catch (err) {
      if (epoch !== this._epoch) return;
      const i2 = this._cardIdx(code);
      if (i2 >= 0) this._patchCard(i2, { pending: false, count: Math.max(0, this.data.behaviors[i2].count - 1) });
      if (err && (err.code === 40401 || err.code === 40001)) this.refreshCards();
    }
  },

  async _handleMinus(code) {
    const idx = this._cardIdx(code); if (idx < 0) return;
    const card = this.data.behaviors[idx];
    if (card.pending || card.count <= 0 || !card.latestRecordId) return;
    if (card.latestDetailSaved) {
      const r = await new Promise((resolve) => wx.showModal({ title: '确认删除', content: '最近一次记录已填写详细信息，删除后不可恢复', confirmColor: '#f87171', success: (res) => resolve(res.confirm), fail: () => resolve(false) }));
      if (!r.confirm) return;
    }
    const epoch = this._epoch;
    this._patchCard(idx, { pending: true, count: card.count - 1 });
    try {
      await recordApi.deleteBehaviorRecord(card.latestRecordId); if (epoch !== this._epoch) return;
      await this.refreshCards();
    } catch (err) {
      if (epoch !== this._epoch) return;
      const i2 = this._cardIdx(code); if (i2 >= 0) this._patchCard(i2, { pending: false, count: this.data.behaviors[i2].count + 1 });
      if (err && err.code === 40401) this.refreshCards();
    }
  },

  // ==================== ABC 弹窗 ====================

  onBehaviorDetail(e) {
    const { behavior } = e.detail;
    if (!this.data.classRecordId || !behavior.count) { wx.showToast({ title: '先点 + 记录一次该行为', icon: 'none' }); return; }
    this.setData({ abcVisible: true, abcBehavior: { behaviorCode: behavior.behaviorCode, behaviorLabel: behavior.behaviorLabel } });
  },
  onAbcClose() { this.setData({ abcVisible: false }); },
  onAbcChanged() { this.refreshCards(); },

  // ==================== 周/月视图 ====================

  async loadWeek() {
    try { const data = await recordApi.getSummary('WEEKLY', this.data.date); this.setData({ weekData: data }); } catch (err) { this.setData({ weekData: null }); }
  },
  async loadMonth() {
    try { const data = await recordApi.getSummary('MONTHLY', this.data.date); this.setData({ monthData: data }); } catch (err) { this.setData({ monthData: null }); }
  },
  onEvalInput(e) { const { field } = e.currentTarget.dataset; this.setData({ [`evaluation.${field}`]: e.detail.value }); },

  /** 全天汇总：加载当日行为统计 */
  async loadDayStats() {
    try {
      const stats = await recordApi.getEvaluationStats('DAILY', this.data.date);
      this.setData({ dayStats: (stats && stats.items) ? stats.items : [] });
    } catch (err) { this.setData({ dayStats: [] }); }
  }
});
