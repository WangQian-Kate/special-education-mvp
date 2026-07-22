// pages/records/records.js
// TAB1 随班记录：日/周/月三视图 + 新行为目录（v2.6：模块/行为/子行为/三状态）
const recordApi = require('../../api/record');
const {
  COURSES, ALL_DAY_COURSE, ENVIRONMENTS, COURSE_DEFAULT_ENV,
  DURATION_PRESETS, DEFAULT_DURATION, DURATION_MAX
} = require('../../utils/constants');
const { today } = require('../../utils/datetime');
const store = require('../../utils/store');

const NOTE_DEBOUNCE_MS = 800;

function idxByCode(list, code) { const i = list.findIndex(function (it) { return it.code === code; }); return i >= 0 ? i : 0; }

function groupByModule(items) {
  var map = {};
  var order = [];
  (items || []).forEach(function (it) {
    if (!map[it.moduleCode]) {
      map[it.moduleCode] = { code: it.moduleCode, name: it.moduleLabel, displayOrder: it.moduleDisplayOrder, behaviors: [] };
      order.push(it.moduleCode);
    }
    // 转换为 behavior-row 期望的格式
    var beh = {
      code: it.code,
      name: it.label,
      displayOrder: it.displayOrder,
      groups: it.groups || [],
      subBehaviors: (it.subBehaviors || []).map(function (s) {
        return {
          code: s.code,
          name: s.label,
          displayOrder: s.displayOrder,
          performanceOptions: s.performanceOptions || []
        };
      }),
      performanceOptions: it.performanceOptions || [],
      counts: {} // { 'subName': { 'statusCode': count } }  稍后从课堂记录填充
    };
    map[it.moduleCode].behaviors.push(beh);
  });
  // 按 displayOrder 排序
  order.forEach(function (k) {
    map[k].behaviors.sort(function (a, b) { return a.displayOrder - b.displayOrder; });
  });
  return order.map(function (k) { return map[k]; }).sort(function (a, b) { return a.displayOrder - b.displayOrder; });
}

Page({
  data: {
    date: '', todayStr: '',
    courses: COURSES, environments: ENVIRONMENTS, durationPresets: DURATION_PRESETS,
    courseIndex: 1, envIndex: 0, durationMinutes: DEFAULT_DURATION._default,
    classRecordId: null,
    // 行为目录模块列表
    modules: [],
    note: '', noteSaveState: '',
    loading: false,
    recordView: 'day',
    weekData: null, monthData: null,
    evalFields: [
      { key: 'emotion', label: '情绪行为' },
      { key: 'adaptation', label: '社会适应能力' },
      { key: 'social', label: '社会交往能力' },
      { key: 'selfMgmt', label: '自我行为管理能力' },
      { key: 'language', label: '语言理解及表达' },
      { key: 'focus', label: '自身/共同专注力' }
    ],
    evaluation: { emotion: '', adaptation: '', social: '', selfMgmt: '', language: '', focus: '' },
    dayStats: [],
    abcVisible: false, abcBehavior: null, abcPerfOptions: []
  },

  onLoad() {
    var t = today();
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
    var view = e.currentTarget.dataset.view;
    if (view === this.data.recordView) return;
    if (this.data.recordView === 'day') this._flushNote();
    this.setData({ recordView: view });
    if (view === 'week') this.loadWeek();
    else if (view === 'month') this.loadMonth();
  },

  // ==================== 日视图 ====================
  async loadDay(date) {
    this._epoch += 1; var epoch = this._epoch; this._createPromise = null;
    this.setData({ loading: true });
    try {
      var list = await recordApi.getDayRecords(date);
      if (epoch !== this._epoch) return;
      this.dayRecords = list || [];
      await this.loadCatalog();
      if (this.dayRecords.length) await this.bindRecord(this.dayRecords[this.dayRecords.length - 1].id, epoch);
    } catch (err) {
      if (epoch !== this._epoch) return;
      this.dayRecords = [];
      this.setData({ classRecordId: null, modules: [], note: '', noteSaveState: '' });
    } finally { if (epoch === this._epoch) this.setData({ loading: false }); }
  },

  /** 加载行为目录（按课程+环境筛选） */
  async loadCatalog() {
    if (this.isAllDay()) return;
    var courseCode = COURSES[this.data.courseIndex].code;
    var envCode = ENVIRONMENTS[this.data.envIndex].code;
    try {
      var catalog = await recordApi.getBehaviorCatalog(courseCode, envCode);
      var modules = groupByModule(catalog);
      this.setData({ modules: modules });
    } catch (err) {
      wx.showToast({ title: '目录加载失败: ' + (err && (err.message || err.code) || '未知'), icon: 'none', duration: 3000 });
      this.setData({ modules: [] });
    }
  },

  async bindRecord(id, epoch) {
    var detail = await recordApi.getClassRecordDetail(id);
    if (epoch !== this._epoch) return;
    var that = this;
    // 并行拉取每个有记录的行为的实际计数
    var cards = detail.behaviorCards || [];
    var countPromises = cards.map(function (c) {
      if (!c.count) return Promise.resolve({ code: c.behaviorCode, counts: {} });
      var beh = that._findBehaviorByCode(c.behaviorCode);
      return recordApi.listBehaviorRecords(id, c.behaviorCode).then(function (recs) {
        var counts = that._countByStatus(recs || [], beh);
        return { code: c.behaviorCode, counts: counts };
      }).catch(function () { return { code: c.behaviorCode, counts: {} }; });
    });
    var results = await Promise.all(countPromises);
    var countMap = {};
    results.forEach(function (r) { countMap[r.code] = r.counts; });
    var modules = this.data.modules.map(function (mod) {
      var behaviors = mod.behaviors.map(function (beh) {
        var serverCounts = countMap[beh.code] || {};
        var oldBeh = that._findOldBehavior(beh.code);
        var merged = oldBeh ? oldBeh.counts : {};
        // 服务端计数合并到本地（服务端优先于默认0）
        Object.keys(serverCounts).forEach(function (k) {
          if (typeof serverCounts[k] === 'object') merged[k] = serverCounts[k];
          else merged[k] = Math.max(merged[k] || 0, serverCounts[k] || 0);
        });
        if (beh.subBehaviors && beh.subBehaviors.length) {
          beh.subBehaviors.forEach(function (sub) {
            if (!merged[sub.name]) merged[sub.name] = { incomplete: 0, assisted: 0, independent: 0 };
          });
        } else if (!merged.incomplete && !merged.assisted && !merged.independent) {
          merged = { incomplete: 0, assisted: 0, independent: 0 };
        }
        return { ...beh, counts: merged };
      });
      return { ...mod, behaviors: behaviors };
    });
    this.setData({
      classRecordId: detail.id,
      courseIndex: idxByCode(COURSES, detail.courseCode),
      envIndex: idxByCode(ENVIRONMENTS, detail.environmentCode),
      durationMinutes: detail.observationDurationMinutes,
      note: detail.overallRemark || '', noteSaveState: '',
      modules: modules
    });
  },

  _countByStatus(recs, beh) {
    var hasSub = beh && beh.subBehaviors && beh.subBehaviors.length;
    if (!hasSub) {
      var counts = { incomplete: 0, assisted: 0, independent: 0 };
      (recs || []).forEach(function (r) {
        var st = (r.statusCode || 'incomplete').toLowerCase();
        if (st !== 'incomplete' && st !== 'assisted' && st !== 'independent') st = 'incomplete';
        counts[st] = (counts[st] || 0) + 1;
      });
      return counts;
    }
    // 有子行为：按子行为名称拆分计数
    var subMap = {};
    beh.subBehaviors.forEach(function (s) { subMap[s.code] = s.name; });
    var counts = {};
    beh.subBehaviors.forEach(function (s) {
      counts[s.name] = { incomplete: 0, assisted: 0, independent: 0 };
    });
    (recs || []).forEach(function (r) {
      var st = (r.statusCode || 'incomplete').toLowerCase();
      if (st !== 'incomplete' && st !== 'assisted' && st !== 'independent') st = 'incomplete';
      var subName = subMap[r.subBehaviorCode] || (r.subBehaviorCode || '');
      if (subName && counts[subName]) {
        counts[subName][st] = (counts[subName][st] || 0) + 1;
      }
    });
    return counts;
  },

  _findOldBehavior(code) {
    for (var i = 0; i < this.data.modules.length; i++) {
      for (var j = 0; j < this.data.modules[i].behaviors.length; j++) {
        if (this.data.modules[i].behaviors[j].code === code) return this.data.modules[i].behaviors[j];
      }
    }
    return null;
  },

  _findBehaviorByCode(code) {
    return this._findOldBehavior(code);
  },

  async resetToEmpty(courseCode) {
    var allDay = courseCode === ALL_DAY_COURSE;
    var envCode = COURSE_DEFAULT_ENV[courseCode] || 'CLASSROOM';
    this.setData({
      classRecordId: null,
      courseIndex: idxByCode(COURSES, courseCode),
      envIndex: idxByCode(ENVIRONMENTS, envCode),
      durationMinutes: allDay ? 480 : (DEFAULT_DURATION[courseCode] || DEFAULT_DURATION._default),
      note: '', noteSaveState: '', modules: []
    });
    if (!allDay) await this.loadCatalog();
  },

  // ==================== 模块折叠 ====================
  onModuleToggle(e) {
    var name = e.detail.moduleName;
    var modules = this.data.modules.map(function (m) {
      if (m.name === name) m.expanded = !m.expanded;
      return m;
    });
    this.setData({ modules: modules });
  },

  // ==================== 状态计数 +/- ====================
  onStatusChange(e) {
    var d = e.detail;
    var that = this;
    // 惰性创建课堂记录 + 调后端 API
    if (d.delta > 0) {
      this.ensureClassRecord().then(function (cid) {
        // 找到 behavior code（从 modules 中查找）
        var behCode = '';
        that.data.modules.some(function (mod) {
          return mod.behaviors.some(function (beh) {
            if (beh.name === d.behaviorName) { behCode = beh.code; return true; }
            return false;
          });
        });
        recordApi.quickAddBehavior(cid, behCode, d.subBehavior || null, d.status).catch(function (err) {
          wx.showToast({ title: '记录失败: ' + ((err && err.message) || '网络异常'), icon: 'none' });
        });
      }).catch(function () {});
    }
    // 乐观更新本地计数
    var modules = this.data.modules.map(function (mod) {
      var behaviors = mod.behaviors.map(function (beh) {
        if (beh.name !== d.behaviorName) return beh;
        var counts = {};
        // deep copy
        if (beh.subBehaviors && beh.subBehaviors.length) {
          (beh.subBehaviors || []).forEach(function (sub) {
            counts[sub.name] = { ...(beh.counts[sub.name] || { incomplete: 0, assisted: 0, independent: 0 }) };
          });
          if (d.subBehavior && counts[d.subBehavior]) {
            counts[d.subBehavior][d.status] = Math.max(0, (counts[d.subBehavior][d.status] || 0) + d.delta);
          }
        } else {
          counts = { incomplete: (beh.counts.incomplete || 0), assisted: (beh.counts.assisted || 0), independent: (beh.counts.independent || 0) };
          counts[d.status] = Math.max(0, (counts[d.status] || 0) + d.delta);
        }
        return { ...beh, counts: counts };
      });
      return { ...mod, behaviors: behaviors };
    });
    this.setData({ modules: modules });
  },

  // ==================== 选择器 ====================
  async onDateChange(e) {
    var date = e.detail.value;
    if (date === this.data.date) return;
    await this._flushNote(); this.setData({ date: date }); this.loadDay(date);
  },

  async onCourseChange(e) {
    var ci = Number(e.detail.value);
    if (ci === this.data.courseIndex) return;
    var code = COURSES[ci].code;
    await this._flushNote();
    this._epoch += 1; var epoch = this._epoch; this._createPromise = null;
    this.setData({ courseIndex: ci });
    if (code === ALL_DAY_COURSE) { await this.resetToEmpty(code, epoch); this.loadDayStats(); return; }
    var matches = this.dayRecords.filter(function (r) { return r.courseCode === code; });
    try {
      if (matches.length) await this.bindRecord(matches[matches.length - 1].id, epoch);
      else await this.resetToEmpty(code, epoch);
    } catch (err) { if (epoch === this._epoch) wx.showToast({ title: '加载失败', icon: 'none' }); }
  },

  onEnvChange(e) {
    if (this.isAllDay()) return;
    var ei = Number(e.detail.value); var prev = this.data.envIndex;
    if (ei === prev) return;
    this.setData({ envIndex: ei });
    this.loadCatalog();
    if (this.data.classRecordId) {
      recordApi.patchClassRecord(this.data.classRecordId, { environmentCode: ENVIRONMENTS[ei].code })
        .catch(function () { /* 静默 */ });
    }
  },

  onDurationChange(e) {
    if (this.isAllDay()) return;
    var preset = DURATION_PRESETS[Number(e.detail.value)];
    if (preset.value === 'custom') {
      var that = this;
      wx.showModal({ title: '自定义观察周期', editable: true, placeholderText: '输入分钟数（1-' + DURATION_MAX + '）',
        success: function (res) {
          if (!res.confirm) return;
          var v = parseInt(String(res.content || '').trim(), 10);
          if (!v || v < 1 || v > DURATION_MAX) { wx.showToast({ title: '请输入1-' + DURATION_MAX + '的整数', icon: 'none' }); return; }
          that._applyDuration(v);
        }
      });
    } else this._applyDuration(preset.value);
  },

  async _applyDuration(min) {
    var prev = this.data.durationMinutes; if (min === prev) return;
    this.setData({ durationMinutes: min });
    if (!this.data.classRecordId) return;
    recordApi.patchClassRecord(this.data.classRecordId, { observationDurationMinutes: min })
      .catch(function () { /* 静默 */ });
  },

  // ==================== 备注（保持不变） ====================
  onNoteInput(e) { this.setData({ note: e.detail.value }); if (this._noteTimer) clearTimeout(this._noteTimer); this._noteTimer = setTimeout(this._saveNote.bind(this), NOTE_DEBOUNCE_MS); },
  onNoteRetryTap() { if (this.data.noteSaveState === 'error') this._saveNote(); },
  _flushNote() { if (this._noteTimer) { clearTimeout(this._noteTimer); this._noteTimer = null; return this._saveNote(); } return Promise.resolve(); },

  async _saveNote(isRetry) {
    if (this._noteSaving || this.isAllDay()) return;
    var value = this.data.note;
    if (!this.data.classRecordId && !value.trim()) return;
    this._noteSaving = true; var epoch = this._epoch;
    this.setData({ noteSaveState: 'saving' });
    try {
      if (this.data.classRecordId) await recordApi.patchClassRecord(this.data.classRecordId, { overallRemark: value || null });
      else await this.ensureClassRecord({ overallRemark: value });
      this._noteSaving = false; if (epoch !== this._epoch) return;
      this.setData({ noteSaveState: 'saved' });
      if (this.data.note !== value) this._saveNote();
    } catch (err) {
      this._noteSaving = false; if (epoch !== this._epoch) return;
      if (!isRetry) setTimeout(function () { if (epoch === this._epoch) this._saveNote(true); }.bind(this), 2000);
      else this.setData({ noteSaveState: 'error' });
    }
  },

  ensureClassRecord(extra) {
    if (this.isAllDay()) return Promise.reject(new Error('全天汇总'));
    if (this.data.classRecordId) return Promise.resolve(this.data.classRecordId);
    if (this._createPromise) return this._createPromise;
    var epoch = this._epoch;
    var payload = {
      recordDate: this.data.date,
      courseCode: COURSES[this.data.courseIndex].code,
      environmentCode: ENVIRONMENTS[this.data.envIndex].code,
      observationDurationMinutes: this.data.durationMinutes, ...extra
    };
    var that = this;
    this._createPromise = recordApi.createClassRecord(payload).then(function (detail) {
      that._createPromise = null;
      if (epoch !== that._epoch) return detail.id;
      that.dayRecords.push(detail);
      that.setData({ classRecordId: detail.id });
      return detail.id;
    }).catch(function (err) { that._createPromise = null; throw err; });
    return this._createPromise;
  },

  /** 点行为名 → 自动创建课堂记录后打开详细记录弹窗 */
  async onBehaviorNameTap(e) {
    var d = e.detail;
    try {
      await this.ensureClassRecord();
      // 从 modules 中查找该行为的 performanceOptions
      var perfOpts = [];
      this.data.modules.some(function (mod) {
        return mod.behaviors.some(function (beh) {
          if (beh.code === d.behaviorCode) { perfOpts = beh.performanceOptions || []; return true; }
          return false;
        });
      });
      this.setData({
        abcVisible: true, abcPerfOptions: perfOpts,
        abcBehavior: { behaviorCode: d.behaviorCode || '', behaviorLabel: d.behaviorName }
      });
    } catch (err) { /* 创建失败 */ }
  },

  async onSubNameTap(e) {
    var d = e.detail;
    try {
      await this.ensureClassRecord();
      var perfOpts = [];
      this.data.modules.some(function (mod) {
        return mod.behaviors.some(function (beh) {
          if (beh.code === d.behaviorCode) {
            (beh.subBehaviors || []).some(function (sub) {
              if (sub.name === d.subBehavior) { perfOpts = sub.performanceOptions || []; return true; }
              return false;
            });
            return true;
          }
          return false;
        });
      });
      this.setData({
        abcVisible: true, abcPerfOptions: perfOpts,
        abcBehavior: { behaviorCode: d.behaviorCode || '', behaviorLabel: d.subBehavior + '（' + d.behaviorName + '）' }
      });
    } catch (err) { /* 创建失败 */ }
  },

  // ==================== 全天汇总 ====================
  onDayStatTap(e) {
    var code = e.currentTarget.dataset.code;
    var label = e.currentTarget.dataset.label;
    this.setData({ abcVisible: true, abcBehavior: { behaviorCode: code, behaviorLabel: label } });
  },
  onAbcClose() { this.setData({ abcVisible: false }); },
  onAbcChanged() { if (this.isAllDay()) this.loadDayStats(); else this.refreshCards(); },
  async refreshCards() { /* 保留 */ },
  onEvalInput(e) { var f = e.currentTarget.dataset.field; this.setData({ ['evaluation.' + f]: e.detail.value }); },
  async loadDayStats() {
    try { var stats = await recordApi.getEvaluationStats('DAILY', this.data.date); this.setData({ dayStats: (stats && stats.items) ? stats.items : [] }); } catch (err) { this.setData({ dayStats: [] }); }
  },

  // ==================== 周/月 ====================
  async loadWeek() { try { var d = await recordApi.getSummary('WEEKLY', this.data.date); this.setData({ weekData: d }); } catch (err) { this.setData({ weekData: null }); } },
  async loadMonth() { try { var d = await recordApi.getSummary('MONTHLY', this.data.date); this.setData({ monthData: d }); } catch (err) { this.setData({ monthData: null }); } }
});
