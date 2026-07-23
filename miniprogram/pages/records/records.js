// pages/records/records.js
// TAB1 随班记录：日/周/月三视图 + 新行为目录（v2.6：模块/行为/子行为/三状态）
const recordApi = require('../../api/record');
const {
  COURSES, ALL_DAY_COURSE, ENVIRONMENTS, COURSE_DEFAULT_ENV,
  DURATION_PRESETS, DEFAULT_DURATION, DURATION_MAX
} = require('../../utils/constants');
const { today } = require('../../utils/datetime');
const store = require('../../utils/store');
var wkLineChart = null;
var moLineChart = null;

function initWkLineChart(canvas, width, height, dpr) {
  if (!canvas || !width || !height) return null;  // ec-canvas 异步初始化时参数可能为空
  var echarts = require('../../components/ec-canvas/echarts');
  var chart = echarts.init(canvas, null, { width: width, height: height, devicePixelRatio: dpr });
  canvas.setChart(chart); wkLineChart = chart;
  chart.setOption({
    grid: { left: 44, right: 16, top: 20, bottom: 36 },
    xAxis: { type: 'category', data: ['一','二','三','四','五'], axisLabel: { fontSize: 10, color: '#9ca3af' }, axisLine: { lineStyle: { color: '#e5e7eb' } } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { fontSize: 10, color: '#9ca3af' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    legend: { data: ['总次数','独立','未完成'], bottom: 0, textStyle: { fontSize: 10, color: '#9ca3af' } },
    series: [
      { name: '总次数', type: 'line', data: [0,0,0,0,0], smooth: true, symbol: 'circle', symbolSize: 5, lineStyle: { color: '#5B9BD5', width: 2 }, itemStyle: { color: '#5B9BD5' } },
      { name: '独立', type: 'line', data: [0,0,0,0,0], smooth: true, symbol: 'circle', symbolSize: 5, lineStyle: { color: '#22c55e', width: 2 }, itemStyle: { color: '#22c55e' } },
      { name: '未完成', type: 'line', data: [0,0,0,0,0], smooth: true, symbol: 'circle', symbolSize: 5, lineStyle: { color: '#5B9BD5', width: 1.5, type: 'dashed' }, itemStyle: { color: '#5B9BD5' } }
    ]
  });
  return chart;
}

function initMoLineChart(canvas, width, height, dpr) {
  if (!canvas || !width || !height) return null;  // ec-canvas 异步初始化时参数可能为空
  var echarts = require('../../components/ec-canvas/echarts');
  var chart = echarts.init(canvas, null, { width: width, height: height, devicePixelRatio: dpr });
  canvas.setChart(chart); moLineChart = chart;
  chart.setOption({
    grid: { left: 44, right: 16, top: 16, bottom: 28 },
    xAxis: { type: 'category', data: ['第1周','第2周','第3周','第4周'], axisLabel: { fontSize: 10, color: '#9ca3af' }, axisLine: { lineStyle: { color: '#e5e7eb' } } },
    yAxis: { type: 'value', min: 0, max: 100, axisLabel: { fontSize: 10, color: '#9ca3af', formatter: '{value}%' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [{ type: 'line', data: [0,0,0,0], smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { color: '#22c55e', width: 2.5 }, itemStyle: { color: '#22c55e' }, areaStyle: { color: 'rgba(34,197,94,0.08)' } }]
  });
  return chart;
}

const NOTE_DEBOUNCE_MS = 800;

function idxByCode(list, code) { const i = list.findIndex(function (it) { return it.code === code; }); return i >= 0 ? i : 0; }

// 子行为缩句映射（去掉亲代行为已有表意，控制在 ≤7 字）
var SUB_SHORTEN = {
  '主动创作，内容与要求一致': '内容与要求一致',
  '安静聆听同伴介绍作品想法': '听同伴介绍作品',
  '听完介绍后给予恰当评价': '给予恰当评价',
  '关注课堂提问简单问题': '关注简单提问',
  '主动合适方式回应提问': '合适方式回应提问',
  '关注课堂提问复杂问题': '关注复杂提问',
  '安静聆听同伴展示作品': '聆听同伴展示',
  '主动配合老师整理用具': '配合老师整理',
  '主动配合组长整理用具': '配合组长整理',
  '向老师提问简单问题': '提问简单问题',
  '坚持等待下一步指令': '等待下一步指令',
  '按要求排队离开教室': '排队离开教室',
  '向老师提问复杂问题': '提问复杂问题',
  '不与同伴大声聊天': '不大声聊天',
  '不与同伴嬉戏打闹': '不嬉戏打闹',
  '在规定时间内吃完': '按时吃完',
  '回应他人合作邀请': '回应合作邀请',
  '理解集体游戏规则': '理解游戏规则',
  '主动合适方式举手': '合适方式举手',
  '不会进入其它班级': '不进入其它班级',
  '较灵活地双手协调': '双手协调'
};

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
          name: SUB_SHORTEN[s.label] || s.label,
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
    wkLoading: false, wkEmpty: true,
    wkOverview: null, wkTotal: 0, wkItems: [],
    wkDist: { incomplete: 0, assisted: 0, independent: 0 },
    wkPctIncomplete: 0, wkPctAssisted: 0, wkPctIndependent: 0,
    wkCourses: [], wkEnvs: [], wkTop6: [],
    wkGoalCount: 0, wkGoalModules: [], wkDailyTrend: [], wkMaxDay: 1,
    wkAbcDist: [],
    wkMaxTop: 1,
    wkWeeklyBreakdown: [],        // 月：per-week 状态拆分
    wkBehDescTrends: [],          // 月：行为表现频次趋势
    wkBehDescMax: 1,             // 月：行为表现最大频次（bar 宽度基准）
    ecWkLine: { onInit: initWkLineChart },
    ecMoLine: { onInit: initMoLineChart },
    wkOffset: 0, wkWeekNum: 0, wkWeekRange: '', wkMonthLabel: '',
    evalFields: [
      { key: 'emotion', label: '情绪行为' },
      { key: 'adaptation', label: '社会适应能力' },
      { key: 'social', label: '社会交往能力' },
      { key: 'selfMgmt', label: '自我行为管理能力' },
      { key: 'language', label: '语言理解及表达' },
      { key: 'focus', label: '自身/共同专注力' }
    ],
    evaluation: { emotion: '', adaptation: '', social: '', selfMgmt: '', language: '', focus: '' },
    evalSaveState: '',
    dayStats: [],
    abcVisible: false, abcBehavior: null, abcPerfOptions: [], abcSubBehaviorCode: ''
  },

  onLoad() {
    var t = today();
    this.dayRecords = []; this._epoch = 0; this._noteTimer = null; this._noteSaving = false; this._createPromise = null;
    this.setData({ date: t, todayStr: t });
    // 恢复今日教师评价
    recordApi.getDailyEvaluation(t).then(function (data) {
      if (data) this.setData({ evaluation: data });
    }.bind(this)).catch(function () {});
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
    if (view === 'week') { this.loadWeek(); }
    else if (view === 'month') { this.loadMonth(); }
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
      // 初始加载：只在当前课程有记录时才绑定
      var curCode = COURSES[this.data.courseIndex].code;
      var myRec = (list || []).filter(function (r) { return r.courseCode === curCode; }).pop();
      if (myRec) await this.bindRecord(myRec.id, epoch);
    } catch (err) {
      if (epoch !== this._epoch) return;
      this.dayRecords = [];
      this.setData({ classRecordId: null, modules: [], note: '', noteSaveState: '' });
    } finally { if (epoch === this._epoch) this.setData({ loading: false }); }
  },

  /** 加载行为目录（按课程+环境筛选） */
  async loadCatalog() {
    var courseCode = this.isAllDay() ? 'ALL_DAY_SUMMARY' : COURSES[this.data.courseIndex].code;
    var envCode = this.isAllDay() ? null : (ENVIRONMENTS[this.data.envIndex] || {code: null}).code;
    try {
      var catalog = await recordApi.getBehaviorCatalog(courseCode, envCode);
      var modules = groupByModule(catalog);
      if (this.isAllDay()) {
        // 全天汇总：聚合当日所有 class_record 的计数
        modules = await this._aggregateAllDayCounts(modules);
      }
      this.setData({ modules: modules });
    } catch (err) {
      wx.showToast({ title: '目录加载失败: ' + (err && (err.message || err.code) || '未知'), icon: 'none', duration: 3000 });
      this.setData({ modules: [] });
    }
  },

  /** 全天汇总：直接用 DAILY 评估接口获取完整状态拆分 */
  async _aggregateAllDayCounts(modules) {
    try {
      // 每次都重新拉当日全部课堂记录，确保增量更新同步
      this.dayRecords = await recordApi.getDayRecords(this.data.date);
      var countMap = {};
      var dayRecords = this.dayRecords || [];
      for (var i = 0; i < dayRecords.length; i++) {
        try {
          var detail = await recordApi.getClassRecordDetail(dayRecords[i].id);
          var cards = detail.behaviorCards || [];
          for (var j = 0; j < cards.length; j++) {
            var c = cards[j];
            if (!countMap[c.behaviorCode]) countMap[c.behaviorCode] = { incomplete: 0, assisted: 0, independent: 0 };
            try {
              var recs = await recordApi.listBehaviorRecords(dayRecords[i].id, c.behaviorCode);
              (recs || []).forEach(function (r) {
                var st = (r.statusCode || 'incomplete').toLowerCase();
                var subKey = r.subBehaviorCode || '';
                // 子行为计数：countMap[parentCode][subName] = { incomplete, assisted, independent }
                if (subKey) {
                  if (!countMap[c.behaviorCode][subKey]) countMap[c.behaviorCode][subKey] = { incomplete: 0, assisted: 0, independent: 0 };
                  if (st === 'independent') countMap[c.behaviorCode][subKey].independent++;
                  else if (st === 'assisted') countMap[c.behaviorCode][subKey].assisted++;
                  else countMap[c.behaviorCode][subKey].incomplete++;
                } else {
                  if (st === 'independent') countMap[c.behaviorCode].independent++;
                  else if (st === 'assisted') countMap[c.behaviorCode].assisted++;
                  else countMap[c.behaviorCode].incomplete++;
                }
              });
            } catch (e2) {
              countMap[c.behaviorCode].independent += c.count;
            }
          }
        } catch (e) { /* skip */ }
      }
      modules.forEach(function (mod) {
        mod.behaviors.forEach(function (beh) {
          var mapData = countMap[beh.code] || { incomplete: 0, assisted: 0, independent: 0 };
          var counts = { incomplete: mapData.incomplete || 0, assisted: mapData.assisted || 0, independent: mapData.independent || 0 };
          // 复制子行为计数
          if (beh.subBehaviors) {
            beh.subBehaviors.forEach(function (sub) {
              var subData = mapData[sub.name] || { incomplete: 0, assisted: 0, independent: 0 };
              counts[sub.name] = { incomplete: subData.incomplete || 0, assisted: subData.assisted || 0, independent: subData.independent || 0 };
            });
          }
          beh.counts = counts;
        });
      });
    } catch (e) { /* skip */ }
    return modules;
  },

  async bindRecord(id, epoch) {
    var detail = await recordApi.getClassRecordDetail(id);
    if (epoch !== this._epoch) return;
    // 课程默认环境优先（用户可手动切环境），再加载目录
    var defaultEnv = COURSE_DEFAULT_ENV[detail.courseCode] || 'CLASSROOM';
    this.setData({
      courseIndex: idxByCode(COURSES, detail.courseCode),
      envIndex: idxByCode(ENVIRONMENTS, defaultEnv),
      durationMinutes: detail.observationDurationMinutes
    });
    if (defaultEnv !== detail.environmentCode) {
      recordApi.patchClassRecord(id, { environmentCode: defaultEnv }).catch(function () {});
    }
    await this.loadCatalog();
    if (epoch !== this._epoch) return;

    var that = this;
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
    await this.loadCatalog();
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
    var behCode = '';
    this.data.modules.some(function (mod) {
      return mod.behaviors.some(function (beh) {
        if (beh.name === d.behaviorName) { behCode = beh.code; return true; }
        return false;
      });
    });

    if (d.delta > 0) {
      this.ensureClassRecord().then(function (cid) {
        recordApi.quickAddBehavior(cid, behCode, d.subBehavior || null, d.status).catch(function (err) {
          wx.showToast({ title: '记录失败: ' + ((err && err.message) || '网络异常'), icon: 'none' });
        });
      }).catch(function () {});
    } else if (d.delta < 0 && this.data.classRecordId) {
      // 减号：删最近一条匹配记录
      var cid = this.data.classRecordId;
      recordApi.listBehaviorRecords(cid, behCode).then(function (recs) {
        var target = null;
        (recs || []).forEach(function (r) {
          var st = (r.statusCode || 'incomplete').toLowerCase();
          var sub = r.subBehaviorCode || '';
          var matchSub = !d.subBehavior || sub === d.subBehavior;
          if (st === d.status && matchSub && (!target || r.id > target.id)) target = r;
        });
        if (!target) return;
        var doDelete = function () {
          recordApi.deleteBehaviorRecord(target.id).catch(function () {});
        };
        if (target.detailSaved) {
          wx.showModal({
            title: '确认删除',
            content: '该次已填写 ABC 详情，删除后不可恢复，确定删除吗？',
            confirmColor: '#f87171',
            success: function (res) { if (res.confirm) doDelete(); }
          });
        } else {
          doDelete();
        }
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

  /** 点行为名 → 自动创建课堂记录后打开详细记录弹窗（全天汇总跳过） */
  async onBehaviorNameTap(e) {
    var d = e.detail;
    if (!this.isAllDay()) {
      try { await this.ensureClassRecord(); } catch (err) { return; }
    }
    var perfOpts = [];
    this.data.modules.some(function (mod) {
      return mod.behaviors.some(function (beh) {
        if (beh.code === d.behaviorCode) { perfOpts = beh.performanceOptions || []; return true; }
        return false;
      });
    });
    this.setData({
      abcVisible: true, abcPerfOptions: perfOpts, abcSubBehaviorCode: '',
      abcBehavior: { behaviorCode: d.behaviorCode || '', behaviorLabel: d.behaviorName }
    });
  },

  async onSubNameTap(e) {
    var d = e.detail;
    if (!this.isAllDay()) {
      try { await this.ensureClassRecord(); } catch (err) { return; }
    }
    var perfOpts = [];
    this.data.modules.some(function (mod) {
      return mod.behaviors.some(function (beh) {
        if (beh.code === d.behaviorCode) {
          (beh.subBehaviors || []).some(function (sub) {
            if (sub.code === d.subBehaviorCode) {
              perfOpts = (sub.performanceOptions && sub.performanceOptions.length) ? sub.performanceOptions : (beh.performanceOptions || []);
              return true;
            }
            return false;
          });
          return true;
        }
        return false;
      });
    });
    this.setData({
      abcVisible: true, abcPerfOptions: perfOpts, abcSubBehaviorCode: d.subBehavior || '',
      abcBehavior: { behaviorCode: d.behaviorCode || '', behaviorLabel: d.subBehavior + '（' + d.behaviorName + '）' }
    });
  },

  // ==================== 全天汇总 ====================
  onDayStatTap(e) {
    var code = e.currentTarget.dataset.code;
    var label = e.currentTarget.dataset.label;
    this.setData({ abcVisible: true, abcBehavior: { behaviorCode: code, behaviorLabel: label }, abcSubBehaviorCode: '' });
  },
  onAbcClose() { this.setData({ abcVisible: false }); },
  onAbcChanged(e) {
    var d = (e && e.detail) || {};
    if (d.delta && d.behaviorCode) { this._applyDelta(d); return; }
    var that = this;
    if (this.isAllDay()) {
      this._aggregateAllDayCounts(this.data.modules).then(function (modules) {
        that.setData({ modules: modules });
      });
    } else if (this.data.classRecordId) {
      // 直接重新绑定课堂记录，完整刷新计数
      var d = (e && e.detail) || {};
	    if (d.delta && d.behaviorCode) { this._applyDelta(d); return; }
	    this.bindRecord(this.data.classRecordId, this._epoch);
    }
  },
  _applyDelta(d) {
    var modules = this.data.modules.map(function (mod) {
      var behaviors = mod.behaviors.map(function (beh) {
        if (beh.code !== d.behaviorCode) return beh;
        var counts = {};
        Object.keys(beh.counts).forEach(function (k) { counts[k] = Object.assign({}, beh.counts[k]); });
        var key = d.subBehavior || '';
        if (key && counts[key]) counts[key][d.status] = (counts[key][d.status] || 0) + d.delta;
        else counts[d.status] = (counts[d.status] || 0) + d.delta;
        return Object.assign({}, beh, { counts: counts });
      });
      return Object.assign({}, mod, { behaviors: behaviors });
    });
    this.setData({ modules: modules });
  },
  async refreshCards() {
    if (!this.data.classRecordId) return;
    var id = this.data.classRecordId;
    var that = this;
    var detail;
    try { detail = await recordApi.getClassRecordDetail(id); } catch (e) { return; }
    if (!detail) return;
    var cards = detail.behaviorCards || [];
    var countPromises = cards.map(function (c) {
      if (!c.count) return Promise.resolve({ code: c.behaviorCode, counts: {} });
      var beh = that._findBehaviorByCode(c.behaviorCode);
      return recordApi.listBehaviorRecords(id, c.behaviorCode).then(function (recs) {
        return { code: c.behaviorCode, counts: that._countByStatus(recs || [], beh) };
      }).catch(function () { return { code: c.behaviorCode, counts: {} }; });
    });
    var results = await Promise.all(countPromises);
    var countMap = {};
    results.forEach(function (r) { countMap[r.code] = r.counts; });
    var modules = this.data.modules.map(function (mod) {
      var behaviors = mod.behaviors.map(function (beh) {
        var serverCounts = countMap[beh.code] || {};
        var merged = {};
        Object.keys(serverCounts).forEach(function (k) {
          if (typeof serverCounts[k] === 'object') merged[k] = serverCounts[k];
          else merged[k] = Math.max((beh.counts && beh.counts[k]) || 0, serverCounts[k] || 0);
        });
        if (beh.subBehaviors && beh.subBehaviors.length) {
          beh.subBehaviors.forEach(function (sub) {
            if (!merged[sub.name]) merged[sub.name] = { incomplete: 0, assisted: 0, independent: 0 };
          });
        } else if (!merged.incomplete && !merged.assisted && !merged.independent) {
          merged = beh.counts || { incomplete: 0, assisted: 0, independent: 0 };
        }
        return { ...beh, counts: merged };
      });
      return { ...mod, behaviors: behaviors };
    });
    this.setData({ modules: modules });
  },
  onEvalInput(e) {
    var f = e.currentTarget.dataset.field;
    this.setData({ ['evaluation.' + f]: e.detail.value, evalSaveState: '' });
    // 自动保存到后端
    clearTimeout(this._evalTimer);
    var that = this;
    this._evalTimer = setTimeout(function () {
      that.setData({ evalSaveState: 'saving' });
      var data = Object.assign({}, that.data.evaluation, { recordDate: that.data.date });
      recordApi.saveDailyEvaluation(data).then(function () {
        that.setData({ evalSaveState: 'saved' });
      }).catch(function () {
        that.setData({ evalSaveState: 'error' });
      });
    }, 800);
  },
  async loadDayStats() {
    try { var stats = await recordApi.getEvaluationStats('DAILY', this.data.date); this.setData({ dayStats: (stats && stats.items) ? stats.items : [] }); } catch (err) { this.setData({ dayStats: [] }); }
  },

  // ==================== 周/月 ====================
  async loadWeek() {
    this._updateWkNav('week'); await this._loadPeriod('WEEKLY');
  },
  async loadMonth() {
    this._updateWkNav('month'); await this._loadPeriod('MONTHLY');
  },

  wkPrev() { this.data.wkOffset--; this.data.recordView === 'week' ? this.loadWeek() : this.loadMonth(); },
  wkNext() { this.data.wkOffset++; this.data.recordView === 'week' ? this.loadWeek() : this.loadMonth(); },

  _updateWkNav(type) {
    var offset = this.data.wkOffset;
    var d = new Date(); d.setDate(d.getDate() + offset * 7);
    if (type === 'week') {
      var day = d.getDay() || 7; var mon = new Date(d); mon.setDate(d.getDate() - day + 1);
      var fri = new Date(mon); fri.setDate(mon.getDate() + 4);
      var fm = function (dt) { return (dt.getMonth() + 1) + '.' + dt.getDate(); };
      var weekNum = Math.ceil((d.getDate() - d.getDay() + 1) / 7) || 1;
      this.setData({ wkWeekNum: weekNum, wkWeekRange: fm(mon) + '-' + fm(fri) });
    } else {
      this.setData({ wkMonthLabel: d.getFullYear() + '年' + (d.getMonth() + 1) + '月' });
    }
  },

  async _loadPeriod(period) {
    this.setData({ wkLoading: true, wkEmpty: true });
    try {
      var refDate = new Date(); refDate.setDate(refDate.getDate() + this.data.wkOffset * 7);
      var ds = refDate.getFullYear() + '-' + String(refDate.getMonth() + 1).padStart(2, '0') + '-' + String(refDate.getDate()).padStart(2, '0');
      var stats = await recordApi.getEvaluationStats(period, ds);
      var items = (stats && stats.items) || [];
      var total = stats ? stats.totalCount : 0;
      var ov = stats ? stats.overview : null;

      // 真实状态计数（后端已提供）
      var incomplete = ov ? (ov.incompleteCount || 0) : 0;
      var assisted = ov ? (ov.assistedCount || 0) : 0;
      var independent = ov ? (ov.independentCount || 0) : 0;
      var dt = incomplete + assisted + independent || 1;

      // 每日趋势（后端已提供）
      var dailyTrend = (stats && stats.dailyTrends) ? stats.dailyTrends : [];
      var that = this;
      var updateWkChart = function () {
        if (!dailyTrend.length) return;
        var comp = that.selectComponent('#wk-line-chart');
        var chart = comp && comp.chart;
        if (!chart) { setTimeout(updateWkChart, 300); return; }
        var lineTotal = dailyTrend.map(function (d) { return d.recordCount || 0; });
        var lineInd = dailyTrend.map(function (d) { return d.independentCount || 0; });
        var lineInc = dailyTrend.map(function (d) { return d.incompleteCount || 0; });
        try { chart.setOption({ series: [{ data: lineTotal }, { data: lineInd }, { data: lineInc }] }); } catch (_) {}
      };
      updateWkChart();

      // 课程/环境统计（后端已提供）
      var crs = (stats && stats.courseStats) ? stats.courseStats.map(function (c) {
        return { name: c.courseLabel, count: c.totalCount, independentRate: c.independentRate };
      }) : [];
      var envs = (stats && stats.environmentStats) ? stats.environmentStats.map(function (e) {
        return { name: e.environmentLabel, count: e.count };
      }) : [];

      // 高频行为 TOP6（带三色状态拆分）
      var top6base = items.slice().sort(function (a, b) { return b.count - a.count; }).slice(0, 6);
      var top6 = top6base.map(function (i) {
        var t = (i.incompleteCount||0) + (i.assistedCount||0) + (i.independentCount||0) || 1;
        return {
          behaviorCode: i.behaviorCode, behaviorLabel: i.behaviorLabel, count: i.count,
          _indPct: Math.round((i.independentCount||0) / t * 100),
          _assPct: Math.round((i.assistedCount||0) / t * 100),
          _inePct: Math.round((i.incompleteCount||0) / t * 100)
        };
      });

      // 月度 per-week 拆分（后端已提供）
      var weeklyBreakdown = (stats && stats.weeklyBreakdown) ? stats.weeklyBreakdown : [];
      var updateMoChart = function () {
        if (!weeklyBreakdown.length) return;
        var comp = that.selectComponent('#mo-line-chart');
        var chart = comp && comp.chart;
        if (!chart) { setTimeout(updateMoChart, 300); return; }
        var moLineData = weeklyBreakdown.map(function (w) { return w.independentRate; });
        try { chart.setOption({ series: [{ data: moLineData }] }); } catch (_) {}
      };
      updateMoChart();

      // ABC 分布（异步拉取）
      var abcDist = [];
      try {
        var abcRes = await recordApi.getAbcDistribution(period, ds);
        abcDist = (abcRes && abcRes.items) ? abcRes.items.map(function (i) {
          return { label: i.functionLabel || i.label, pct: i.percentage || 0 };
        }) : [];
      } catch (e) { /* 静默 */ }

      var maxDay = Math.max.apply(null, dailyTrend.map(function (d) { return d.recordCount || 0; }).concat([1]));
      var maxTop = top6.length ? top6[0].count : 1;

      this.setData({
        wkLoading: false, wkEmpty: !items.length,
        wkOverview: ov, wkTotal: total, wkItems: items,
        wkDist: { incomplete: incomplete, assisted: assisted, independent: independent },
        wkPctIncomplete: Math.round(incomplete / dt * 100),
        wkPctAssisted: Math.round(assisted / dt * 100),
        wkPctIndependent: Math.round(independent / dt * 100),
        wkTop6: top6,
        wkCourses: crs, wkEnvs: envs,
        wkGoalCount: ov ? (ov.trainingGoalCount || 0) : 0,
        wkGoalModules: [], wkDailyTrend: dailyTrend, wkMaxDay: maxDay,
        wkAbcDist: abcDist,
        wkMaxTop: maxTop,
        wkWeeklyBreakdown: weeklyBreakdown,
        wkBehDescTrends: [], wkBehDescMax: 1
      });
    } catch (err) {
      this.setData({ wkLoading: false, wkEmpty: true });
    }
  },

  async _loadPerBehaviorStatus(top6) {
    var that = this;
    try {
      var dayRecs = await recordApi.getDayRecords(this.data.date);
      var results = await Promise.all(top6.map(function (item) {
        return Promise.all((dayRecs || []).map(function (r) {
          return recordApi.listBehaviorRecords(r.id, item.behaviorCode).catch(function () { return []; });
        })).then(function (allRecs) {
          var ind = 0, ass = 0, ine = 0;
          allRecs.forEach(function (recs) {
            (recs || []).forEach(function (r) {
              var st = (r.statusCode || '').toLowerCase();
              if (st === 'independent') ind++;
              else if (st === 'assisted') ass++;
              else ine++;
            });
          });
          var t = ind + ass + ine || 1;
          return { ...item, _indPct: Math.round(ind / t * 100), _assPct: Math.round(ass / t * 100), _inePct: Math.round(ine / t * 100) };
        });
      }));
      // 按 count 降序排列
      results.sort(function (a, b) { return b.count - a.count; });
      var max = results.length ? results[0].count : 1;
      that.setData({ wkTop6: results, wkMaxTop: max });
    } catch (e) { /* skip */ }
  }
});
