// components/abc-modal/abc-modal.js
// ABC 详录半屏弹窗：上部时间列表 + 下部详情表单同页展示
// 打开时自动选中最新一条；行为功能/辅助方式包在折叠面板"行为功能与辅助方式"中
// 辅助方式勾选不强制填内容；行为功能选填（后端表暂空，传 null 或不选）

const recordApi = require('../../api/record');
const { ASSISTANCE_GROUPS, FUNCTIONS } = require('../../utils/constants');
const { formatTime, formatIsoCst, isFutureTime, nowHhmm } = require('../../utils/datetime');

function buildAssistances() {
  const rows = [];
  ASSISTANCE_GROUPS.forEach((g) => {
    g.items.forEach((it, i) => {
      rows.push({ group: g.group, groupFirst: i === 0, code: it.code, label: it.label, checked: false });
    });
  });
  return rows;
}

function funcIndex(code) {
  const i = FUNCTIONS.findIndex((f) => f.code === code);
  return i >= 0 ? i : -1;
}

Component({
  properties: {
    visible: { type: Boolean, value: false },
    classRecordId: { type: null, value: null },
    recordDate: { type: String, value: '' },
    behavior: { type: null, value: null },
    allDay: { type: Boolean, value: false },
    perfOptions: { type: Array, value: [] },
    subBehaviorCode: { type: String, value: '' }
  },

  observers: {
    visible(visible) {
      if (visible && this.properties.behavior && (this.properties.classRecordId || this.properties.allDay)) {
        this.setData({
          supplementOpen: false, perfChecks: [], otherText: '',
          supplementTime: nowHhmm(), foldOpen: false,
          allDayClassIds: [], loadingList: true,
          durationIndex: 0, durationLabel: '选填', 'form.durationMinutes': ''
        });
        this.loadList();
        this.loadAbcTags();
      }
    },
    'behavior.behaviorCode': function () {
      this.setData({ perfChecks: [], otherText: '', currentRecordId: null });
    }
  },

  data: {
    records: [], loadingList: false,
    supplementOpen: false, supplementTime: '',
    suppStatusIdx: 0, suppStatusCode: '', suppStatusLabel: '选择状态 ▾',
    suppStatusRange: ['选择状态 ▾', '未完成', '辅助完成', '独立完成'],
    currentRecordId: null, formTimeText: '',
    form: { durationMinutes: '', antecedentText: '', behaviorDescription: '', consequenceText: '', assistanceResultText: '' },
    assistances: buildAssistances(),
    perfChecks: [], otherText: '',
    functionIndex: -1, functionLabel: '请选择行为功能...',
    functionRange: ['未选择', '获取关注', '获取实物', '逃避', '感官刺激'],
    foldOpen: false, currentRecordSaved: false, saving: false,
    durationOptions: ['选填', '1分钟', '3分钟', '5分钟', '10分钟', '自定义'],
    durationIndex: 0, durationLabel: '选填',
    abcAntecedentOpts: [], abcConsequenceOpts: [],
    antecedentChecks: {}, consequenceChecks: {},
    antecedentOtherText: '', consequenceOtherText: ''
  },

  methods: {
    // ==================== 时间列表 ====================

    async loadList() {
      this.setData({ loadingList: true });
      try {
        let list;
        if (this.properties.allDay) {
          const dayRecords = await recordApi.getDayRecords(this.properties.recordDate);
          const all = [];
          const crs = dayRecords || [];
          for (let i = 0; i < crs.length; i++) {
            try {
              const recs = await recordApi.listBehaviorRecords(crs[i].id, this.properties.behavior.behaviorCode);
              var mapped = (recs || []).map(function (r) { return { id: r.id, occurredAt: r.occurredAt, detailSaved: r.detailSaved, statusCode: r.statusCode, subBehaviorCode: r.subBehaviorCode, _classRecordId: crs[i].id }; });
              for (var j = 0; j < mapped.length; j++) all.push(mapped[j]);
            } catch (e) {}
          }
          all.sort(function (a, b) { return new Date(b.occurredAt) - new Date(a.occurredAt); });
          var idSet = {}, ids = [];
          for (var k = 0; k < all.length; k++) { if (!idSet[all[k]._classRecordId]) { idSet[all[k]._classRecordId] = true; ids.push(all[k]._classRecordId); } }
          this.setData({ allDayClassIds: ids });
          list = all;
        } else {
          list = await recordApi.listBehaviorRecords(this.properties.classRecordId, this.properties.behavior.behaviorCode);
        }
        if (this.properties.subBehaviorCode) {
          list = (list || []).filter(function (r) { return r.subBehaviorCode === this.properties.subBehaviorCode; }.bind(this));
        }
        const records = (list || []).map(function (r) {
          var st = (r.statusCode || 'incomplete').toLowerCase();
          return { id: r.id, timeText: formatTime(r.occurredAt), detailSaved: r.detailSaved, statusCode: st };
        });
        records.sort(function (a, b) { return (b.id - a.id); });
        this.setData({ records });
        if (records.length) this.openForm(records[0].id);
      } catch (err) {}
      this.setData({ loadingList: false });
    },

    onRecordTap(e) { this.openForm(e.currentTarget.dataset.id); },

    async onDeleteTap(e) {
      const id = Number(e.currentTarget.dataset.id);
      const saved = !!(this.data.currentRecordSaved);
      if (!id) { wx.showToast({ title: '无法定位该记录', icon: 'none' }); return; }
      if (saved) {
        const confirmed = await new Promise((resolve) => {
          wx.showModal({
            title: '确认删除', content: '该次已填写 ABC 详情，删除后不可恢复，确定删除吗？',
            confirmColor: '#f87171', success: (res) => resolve(res.confirm), fail: () => resolve(false)
          });
        });
        if (!confirmed) return;
      }
      // 本地减数：找到被删记录的状态
      var delRec = this.data.records.find(function (r) { return r.id === id; });
      var delStatus = delRec ? delRec.statusCode : 'incomplete';
      this.triggerEvent('changed', {
        delta: -1,
        behaviorCode: this.properties.behavior.behaviorCode,
        subBehavior: this.properties.subBehaviorCode || '',
        status: delStatus
      });
      this.setData({ currentRecordId: null });
      wx.showToast({ title: '已删除', icon: 'success' });
      var that = this;
      recordApi.deleteBehaviorRecord(id).then(function () { that.loadList(); }).catch(function () {});
    },

    // ==================== 补记 ====================

    onSupplementToggle() {
      this.setData({ supplementOpen: !this.data.supplementOpen, supplementTime: nowHhmm(), suppStatusIdx: 0, suppStatusCode: '', suppStatusLabel: '选择状态 ▾' });
    },
    onSupplementTimeChange(e) { this.setData({ supplementTime: e.detail.value }); },
    onSuppStatusChange(e) {
      var idx = Number(e.detail.value);
      var codes = ['', 'incomplete', 'assisted', 'independent'];
      this.setData({ suppStatusIdx: idx, suppStatusCode: codes[idx], suppStatusLabel: this.data.suppStatusRange[idx] });
    },

    async onSupplementConfirm() {
      if (!this.data.suppStatusIdx) { wx.showToast({ title: '请先选择行为状态', icon: 'none' }); return; }
      const { recordDate, allDay } = this.properties;
      const hhmm = this.data.supplementTime;
      if (isFutureTime(recordDate, hhmm)) { wx.showToast({ title: '补记时间不能晚于当前（' + nowHhmm() + '）', icon: 'none', duration: 2500 }); return; }
      // 先关弹窗+本地加数，用户感知即时
      var delta = { delta: 1, behaviorCode: this.properties.behavior.behaviorCode, subBehavior: this.properties.subBehaviorCode || '', status: this.data.suppStatusCode };
      this.setData({ supplementOpen: false });
      this.triggerEvent('changed', delta);
      wx.showToast({ title: '已补记', icon: 'success', duration: 1000 });
      // 后端操作全放后台
      var that = this;
      (async function () {
        try {
          let crId = that.properties.classRecordId;
          if (!crId) {
            const detail = await recordApi.createClassRecord({ recordDate, courseCode: 'OTHER', environmentCode: 'OTHER', observationDurationMinutes: allDay ? 480 : 40 });
            crId = detail.id;
          }
          var occurredAt = formatIsoCst(recordDate, hhmm);
          await recordApi.supplementBehavior(crId, that.properties.behavior.behaviorCode, occurredAt, that.data.suppStatusCode, that.properties.subBehaviorCode || null);
          that.loadList();
        } catch (err) {}
      })();
    },

    // ==================== 详情表单 ====================

    async openForm(recordId) {
      try {
        const d = await recordApi.getBehaviorDetail(recordId);
        const assistances = buildAssistances().map((row) => {
          const hit = (d.assistances || []).find(function (a) { return a.code === row.code; });
          return { group: row.group, groupFirst: row.groupFirst, code: row.code, label: row.label, checked: !!hit };
        });
        const rec = this.data.records.find((r) => r.id === recordId);
        const funcIdx = funcIndex(d.functionCode);
        const savedLabels = (d.behaviorDescription || '').split('、').filter(Boolean);
        const opts = this.properties.perfOptions || [];
        const perfChecks = opts.map(function (o) {
          return { code: o.code, label: o.label, checked: savedLabels.indexOf(o.label) >= 0, custom: o.requiresCustomText };
        });
        // 非标准选项的文字自动作为"其它"内容回显，有则勾选自定义项
        var unmatched = savedLabels.filter(function (l) { return !opts.some(function (o) { return o.label === l; }); });
        var bOtherText = unmatched.join('、');
        if (bOtherText) {
          perfChecks.forEach(function (c) { if (c.custom) c.checked = true; });
        }
        // 初始化持续时间下拉
        var durVal = d.durationMinutes;
        var durIdx = 0, durLabel = '选填';
        if (durVal === 1) { durIdx = 1; durLabel = '1分钟'; }
        else if (durVal === 3) { durIdx = 2; durLabel = '3分钟'; }
        else if (durVal === 5) { durIdx = 3; durLabel = '5分钟'; }
        else if (durVal === 10) { durIdx = 4; durLabel = '10分钟'; }
        else if (durVal != null && durVal > 0) { durIdx = 5; durLabel = '自定义'; }
        // 初始化 A/C 标签勾选
        const antChecks = {};
        var antOtherText = '';
        (d.antecedentSelections || []).forEach(function (c) {
          if (c.code === 'OTHER') { antChecks['_A_OTHER_'] = true; antOtherText = c.customText || ''; }
          else antChecks[c.code || c] = true;
        });
        const conChecks = {};
        var conOtherText = '';
        (d.consequenceSelections || []).forEach(function (c) {
          if (c.code === 'OTHER') { conChecks['_C_OTHER_'] = true; conOtherText = c.customText || ''; }
          else conChecks[c.code || c] = true;
        });
        this.setData({
          currentRecordId: recordId, currentRecordSaved: rec ? rec.detailSaved : false,
          formTimeText: formatTime(d.occurredAt),
          functionIndex: funcIdx, functionLabel: funcIdx >= 0 ? this.data.functionRange[funcIdx + 1] : this.data.functionRange[0],
          perfChecks: perfChecks, otherText: bOtherText,
          form: { durationMinutes: d.durationMinutes == null ? '' : String(d.durationMinutes), antecedentText: d.antecedentText || '', behaviorDescription: d.behaviorDescription || '', consequenceText: d.consequenceText || '', assistanceResultText: d.assistanceResultText || '' },
          assistances,
          durationIndex: durIdx, durationLabel: durLabel,
          antecedentChecks: antChecks, consequenceChecks: conChecks,
          antecedentOtherText: antOtherText, consequenceOtherText: conOtherText
        });
      } catch (err) {
        console.error('[abc-modal] openForm 加载失败:', err);
      }
    },

    onFormInput(e) { const { field } = e.currentTarget.dataset; this.setData({ ['form.' + field]: e.detail.value }); },

    onDurationChange(e) {
      var idx = Number(e.detail.value);
      var mins = [null, 1, 3, 5, 10, null][idx];
      this.setData({
        durationIndex: idx,
        durationLabel: this.data.durationOptions[idx],
        'form.durationMinutes': mins != null ? String(mins) : ''
      });
    },
    onAssistToggle(e) { const { index } = e.currentTarget.dataset; this.setData({ ['assistances[' + index + '].checked']: !this.data.assistances[index].checked }); },
    onFunctionChange(e) {
      const idx = Number(e.detail.value) - 1;
      this.setData({ functionIndex: idx, functionLabel: idx >= 0 ? this.data.functionRange[idx + 1] : this.data.functionRange[0] });
    },
    onPerfToggle(e) { var idx = Number(e.currentTarget.dataset.index); this.setData({ ['perfChecks[' + idx + '].checked']: !this.data.perfChecks[idx].checked }); },
    onOtherTextInput(e) { this.setData({ otherText: e.detail.value }); },

    async onSave() {
      if (this.data.saving) return;
      const f = this.data.form;
      let durationMinutes = null;
      const dText = String(f.durationMinutes || '').trim();
      if (dText) { const v = parseInt(dText, 10); if (!v || v < 1 || String(v) !== dText) { wx.showToast({ title: '持续时间请输入正整数（分钟）', icon: 'none' }); return; } durationMinutes = v; }
      const funcIdx = this.data.functionIndex;
      const functionCode = (funcIdx >= 0 && FUNCTIONS[funcIdx]) ? FUNCTIONS[funcIdx].code : null;
      const checked = this.data.assistances.filter(function (a) { return a.checked; });
      var perfLabels = this.data.perfChecks.filter(function (c) { return c.checked && !c.custom; }).map(function (c) { return c.label; });
      if (this.data.otherText && this.data.otherText.trim()) perfLabels.push(this.data.otherText.trim());
      var behDesc = perfLabels.join('、') || null;
      this.setData({ saving: true });
      try {
        // 收集 A/C 标签勾选（"其它"输入框有字=已选，自动映射为真实 OTHER code）
        var antOtherCode = this.data.abcAntecedentOpts.filter(function (o) { return o.requiresCustomText; })[0];
        var antOtherRealCode = antOtherCode ? antOtherCode.code : '_A_OTHER_';
        var antOtherEffective = this.data.antecedentChecks[antOtherRealCode] || (this.data.antecedentOtherText && this.data.antecedentOtherText.trim().length > 0);
        var antecedentSelections = Object.keys(this.data.antecedentChecks).filter(function (k) { return this.data.antecedentChecks[k] && k !== antOtherRealCode; }.bind(this)).map(function (c) { return { code: c }; });
        if (antOtherEffective) {
          var item = { code: 'OTHER' };
          if (this.data.antecedentOtherText.trim()) item.customText = this.data.antecedentOtherText.trim();
          antecedentSelections.push(item);
        }
        var conOtherCode = this.data.abcConsequenceOpts.filter(function (o) { return o.requiresCustomText; })[0];
        var conOtherRealCode = conOtherCode ? conOtherCode.code : '_C_OTHER_';
        var conOtherEffective = this.data.consequenceChecks[conOtherRealCode] || (this.data.consequenceOtherText && this.data.consequenceOtherText.trim().length > 0);
        var consequenceSelections = Object.keys(this.data.consequenceChecks).filter(function (k) { return this.data.consequenceChecks[k] && k !== conOtherRealCode; }.bind(this)).map(function (c) { return { code: c }; });
        if (conOtherEffective) {
          var item2 = { code: 'OTHER' };
          if (this.data.consequenceOtherText.trim()) item2.customText = this.data.consequenceOtherText.trim();
          consequenceSelections.push(item2);
        }
        await recordApi.saveBehaviorDetail(this.data.currentRecordId, {
          durationMinutes, stageCode: null,
          antecedentText: f.antecedentText || null, antecedentSelections: antecedentSelections,
          behaviorDescription: behDesc,
          consequenceText: f.consequenceText || null, consequenceSelections: consequenceSelections,
          functionCode: functionCode,
          assistances: checked.map(function (a) { return { code: a.code }; }), assistanceResultText: f.assistanceResultText || null
        });
        wx.showToast({ title: '已保存', icon: 'success' });
        this.triggerEvent('changed');
        const records = this.data.records.map(function (r) { if (r.id === this.data.currentRecordId) { r.detailSaved = true; } return r; }.bind(this));
        this.setData({ records }); this.triggerEvent('close');
      } catch (err) { wx.showToast({ title: '保存失败: ' + ((err && err.message) || '网络异常'), icon: 'none' }); }
      this.setData({ saving: false });
    },

    toggleFold() { this.setData({ foldOpen: !this.data.foldOpen }); },
    onClose() { this.triggerEvent('close'); },
    noop() {},

    // ==================== ABC 标签 ====================

    async loadAbcTags() {
      if (this.data.abcAntecedentOpts.length) return;
      await new Promise(function (resolve) { wx.nextTick(resolve); });
      try {
        if (typeof recordApi.getAbcTags !== 'function') { console.error('[abc-modal] recordApi.getAbcTags 未定义'); return; }
        const dict = await recordApi.getAbcTags();
        console.log('[abc-modal] abc-tags loaded:', dict ? 'OK' : 'empty');
        // 每组取第一个非 OTHER 选项 + 末尾固定追加"其它"
        var flatAnte = (dict && dict.antecedentGroups || []).map(function (g) {
          var first = (g.options || []).filter(function (o) { return !o.requiresCustomText; })[0];
          return first ? { code: first.code, label: first.label, requiresCustomText: false } : null;
        }).filter(Boolean);
        flatAnte.push({ code: '_A_OTHER_', label: '其它', requiresCustomText: true });
        var flatConse = (dict && dict.consequenceGroups || []).map(function (g) {
          var first = (g.options || []).filter(function (o) { return !o.requiresCustomText; })[0];
          return first ? { code: first.code, label: first.label, requiresCustomText: false } : null;
        }).filter(Boolean);
        flatConse.push({ code: '_C_OTHER_', label: '其它', requiresCustomText: true });
        console.log('[abc-modal] flat A:', flatAnte.length, 'flat C:', flatConse.length);
        this.setData({ abcAntecedentOpts: flatAnte, abcConsequenceOpts: flatConse });
      } catch (err) {
        console.error('[abc-modal] loadAbcTags 失败:', err);
      }
    },

    onAntecedentToggle(e) {
      const { code } = e.currentTarget.dataset;
      const checks = this.data.antecedentChecks;
      checks[code] = !checks[code];
      this.setData({ antecedentChecks: checks });
    },

    onConsequenceToggle(e) {
      const { code } = e.currentTarget.dataset;
      const checks = this.data.consequenceChecks;
      checks[code] = !checks[code];
      this.setData({ consequenceChecks: checks });
    },

    onAntecedentOtherInput(e) {
      var val = e.detail.value;
      var checks = this.data.antecedentChecks;
      var otherCode = this.data.abcAntecedentOpts.filter(function (o) { return o.requiresCustomText; })[0];
      if (otherCode) { checks[otherCode.code] = val && val.trim().length > 0; }
      this.setData({ antecedentOtherText: val, antecedentChecks: checks });
    },
    onConsequenceOtherInput(e) {
      var val = e.detail.value;
      var checks = this.data.consequenceChecks;
      var otherCode = this.data.abcConsequenceOpts.filter(function (o) { return o.requiresCustomText; })[0];
      if (otherCode) { checks[otherCode.code] = val && val.trim().length > 0; }
      this.setData({ consequenceOtherText: val, consequenceChecks: checks });
    }
  }
});
