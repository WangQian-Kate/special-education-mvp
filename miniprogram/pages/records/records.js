// pages/records/records.js
// TAB1 随班记录（阶段二：本地计数跑通交互；阶段三接后端新增/查询接口）
const mockBehaviors = require('../../mock/behaviors');
const { COURSES, ENVIRONMENTS } = require('../../utils/constants');
const store = require('../../utils/store');

/** 返回 YYYY-MM-DD */
function today() {
  const d = new Date();
  const p = (n) => (n < 10 ? '0' + n : '' + n);
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

Page({
  data: {
    date: '',
    courses: COURSES,
    environments: ENVIRONMENTS,
    courseIndex: 0,
    envIndex: 0,
    note: '',
    behaviors: []
  },

  onLoad() {
    this.setData({
      date: today(),
      // 深拷贝，避免多次进入页面共享 mock 引用
      behaviors: mockBehaviors.map((b) => ({ ...b }))
    });
  },

  onShow() {
    if (!store.getTeacherId()) {
      wx.reLaunch({ url: '/pages/login/login' });
    }
  },

  onDateChange(e) {
    this.setData({ date: e.detail.value });
    // TODO 阶段三：按日期拉取当日记录
  },

  onCourseChange(e) {
    this.setData({ courseIndex: Number(e.detail.value) });
  },

  onEnvChange(e) {
    this.setData({ envIndex: Number(e.detail.value) });
  },

  onNoteInput(e) {
    this.setData({ note: e.detail.value });
  },

  /** 行为计数 +/-（behavior-counter 组件上抛） */
  onCountChange(e) {
    const { id, delta } = e.detail;
    const behaviors = this.data.behaviors.map((b) =>
      b.id === id ? { ...b, count: Math.max(0, b.count + delta) } : b
    );
    this.setData({ behaviors });
    // TODO 阶段三：防抖合并后 POST /behavior-logs
  },

  /** 点击行为卡片 → ABC 详录（阶段三实现 abc-modal） */
  onBehaviorDetail(e) {
    const { behavior } = e.detail;
    wx.showToast({ title: `${behavior.name}：ABC 详录（阶段三）`, icon: 'none' });
  }
});
