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
      rows.push({
        group: g.group,
        groupFirst: i === 0,
        code: it.code,
        label: it.label,
        checked: false
      });
    });
  });
  return rows;
}

/** 功能编码 → picker index */
function funcIndex(code) {
  const i = FUNCTIONS.findIndex((f) => f.code === code);
  return i >= 0 ? i : -1;      // -1 = 未选择
}

Component({
  properties: {
    visible: { type: Boolean, value: false },
    classRecordId: { type: null, value: null },
    recordDate: { type: String, value: '' },
    behavior: { type: null, value: null }
  },

  observers: {
    visible(visible) {
      if (visible && this.properties.behavior && this.properties.classRecordId) {
        this.setData({
          supplementOpen: false,
          supplementTime: nowHhmm(),
          currentRecordId: null,
          foldOpen: false
        });
        this.loadList();
      }
    }
  },

  data: {
    records: [],
    loadingList: false,
    // 补记
    supplementOpen: false,
    supplementTime: '',
    // 详情表单
    currentRecordId: null,
    formTimeText: '',
    form: {
      durationMinutes: '',
      antecedentText: '',
      behaviorDescription: '',
      consequenceText: '',
      assistanceResultText: ''
    },
    assistances: buildAssistances(),
    // 行为功能
    functionIndex: -1,
    functionLabel: '请选择行为功能...',
    functionRange: ['未选择', '获得注意', '获得物品/活动', '逃避/回避', '感觉刺激'],
    // 折叠面板
    foldOpen: true,
    // 当前选中记录的详录状态（删除确认用）
    currentRecordSaved: false,
    saving: false
  },

  methods: {
    // ==================== 时间列表 ====================

    async loadList() {
      this.setData({ loadingList: true });
      try {
        const list = await recordApi.listBehaviorRecords(
          this.properties.classRecordId,
          this.properties.behavior.behaviorCode
        );
        const records = (list || []).map((r) => ({
          id: r.id,
          timeText: formatTime(r.occurredAt),
          detailSaved: r.detailSaved
        }));
        this.setData({ records });
        // 自动选中最新一条（列表按时间升序，取最后一条=最新）
        if (records.length) this.openForm(records[records.length - 1].id);
      } catch (err) {
        // request.js 已 toast
      }
      this.setData({ loadingList: false });
    },

    onRecordTap(e) {
      this.openForm(e.currentTarget.dataset.id);
    },

    async onDeleteTap(e) {
      const id = Number(e.currentTarget.dataset.id);
      // dataset 值为字符串，需显式转换
      const saved = e.currentTarget.dataset.saved === 'true';
      if (!id) { wx.showToast({ title: '无法定位该记录', icon: 'none' }); return; }
      const confirmed = await new Promise((resolve) => {
        wx.showModal({
          title: '确认删除',
          content: saved
            ? '该次已填写 ABC 详情，删除后不可恢复，确定删除吗？'
            : '确定删除这次记录吗？',
          confirmColor: '#f87171',
          success: (res) => resolve(res.confirm),
          fail: () => resolve(false)
        });
      });
      if (!confirmed) return;
      try {
        await recordApi.deleteBehaviorRecord(id);
        this.triggerEvent('changed');
        await this.loadList();
        wx.showToast({ title: '已删除', icon: 'success' });
      } catch (err) { /* 已 toast */ }
    },

    // ==================== 补记 ====================

    onSupplementToggle() {
      this.setData({ supplementOpen: !this.data.supplementOpen, supplementTime: nowHhmm() });
    },

    onSupplementTimeChange(e) {
      this.setData({ supplementTime: e.detail.value });
    },

    async onSupplementConfirm() {
      const { recordDate } = this.properties;
      const hhmm = this.data.supplementTime;
      if (isFutureTime(recordDate, hhmm)) {
        wx.showToast({ title: '补记时间不能晚于当前时刻', icon: 'none' });
        return;
      }
      try {
        const result = await recordApi.supplementBehavior(
          this.properties.classRecordId,
          this.properties.behavior.behaviorCode,
          formatIsoCst(recordDate, hhmm)
        );
        this.triggerEvent('changed');
        this.setData({ supplementOpen: false });
        await this.loadList();
        this.openForm(result.record.id);
      } catch (err) { /* 已 toast */ }
    },

    // ==================== 详情表单 ====================

    async openForm(recordId) {
      try {
        const d = await recordApi.getBehaviorDetail(recordId);
        const assistances = buildAssistances().map((row) => {
          const hit = (d.assistances || []).find((a) => a.code === row.code);
          return hit ? { ...row, checked: true } : row;
        });
        const rec = this.data.records.find((r) => r.id === recordId);
        const funcIdx = funcIndex(d.functionCode);
        this.setData({
          currentRecordId: recordId,
          currentRecordSaved: rec ? rec.detailSaved : false,
          formTimeText: formatTime(d.occurredAt),
          functionIndex: funcIdx,
          functionLabel: funcIdx >= 0 ? this.data.functionRange[funcIdx + 1] : this.data.functionRange[0],
          form: {
            durationMinutes: d.durationMinutes == null ? '' : String(d.durationMinutes),
            antecedentText: d.antecedentText || '',
            behaviorDescription: d.behaviorDescription || '',
            consequenceText: d.consequenceText || '',
            assistanceResultText: d.assistanceResultText || ''
          },
          assistances
        });
      } catch (err) { /* 已 toast */ }
    },

    onFormInput(e) {
      const { field } = e.currentTarget.dataset;
      this.setData({ [`form.${field}`]: e.detail.value });
    },

    onAssistToggle(e) {
      const { index } = e.currentTarget.dataset;
      this.setData({ [`assistances[${index}].checked`]: !this.data.assistances[index].checked });
    },

    onFunctionChange(e) {
      const idx = Number(e.detail.value) - 1;  // range[0]="未选择" → -1
      this.setData({
        functionIndex: idx,
        functionLabel: idx >= 0 ? this.data.functionRange[idx + 1] : this.data.functionRange[0]
      });
    },

    toggleFold() {
      this.setData({ foldOpen: !this.data.foldOpen });
    },

    async onSave() {
      if (this.data.saving) return;
      const f = this.data.form;
      // 持续时间选填
      let durationMinutes = null;
      const dText = String(f.durationMinutes || '').trim();
      if (dText) {
        const v = parseInt(dText, 10);
        if (!v || v < 1 || String(v) !== dText) {
          wx.showToast({ title: '持续时间请输入正整数（分钟）', icon: 'none' });
          return;
        }
        durationMinutes = v;
      }
      // 行为功能/辅助方式后端字典暂未就绪，先传 null/[]，等后端建表后放开
      this.setData({ saving: true });
      try {
        await recordApi.saveBehaviorDetail(this.data.currentRecordId, {
          durationMinutes,
          stageCode: null,
          antecedentText: f.antecedentText || null,
          behaviorDescription: f.behaviorDescription || null,
          consequenceText: f.consequenceText || null,
          functionCode: null,
          assistances: [],
          assistanceResultText: f.assistanceResultText || null
        });
        wx.showToast({ title: '已保存', icon: 'success' });
        this.triggerEvent('changed');
        // 本地把该条置为已详录
        const records = this.data.records.map((r) =>
          r.id === this.data.currentRecordId ? { ...r, detailSaved: true } : r
        );
        this.setData({ records });
      } catch (err) {
        wx.showToast({ title: '保存失败: ' + ((err && err.message) || '网络异常'), icon: 'none' });
      }
      this.setData({ saving: false });
    },

    onClose() {
      this.triggerEvent('close');
    },

    noop() {}
  }
});
