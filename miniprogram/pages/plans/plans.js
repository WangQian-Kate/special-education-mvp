// pages/plans/plans.js
// TAB2 训练计划（已接后端 PATCH 实时保存）
const planApi = require('../../api/plan');
const store = require('../../utils/store');

const STATUS_LIST = ['未开始', '进行中', '已完成'];
const STATUS_MAP = { 'NOT_STARTED': '未开始', 'IN_PROGRESS': '进行中', 'COMPLETED': '已完成' };
const STATUS_REV = { '未开始': 'NOT_STARTED', '进行中': 'IN_PROGRESS', '已完成': 'COMPLETED' };
const LEVELS = ['A', 'B', 'C', 'D', 'E', 'F'];
const PHASES = ['1', '2', '3'];

Page({
  data: {
    categories: [],
    activeCategory: 0,
    statusList: STATUS_LIST,
    activeStatus: '',
    keyword: '',
    levels: LEVELS,
    phases: PHASES,
    filteredItems: [],
    totalCount: 0,
    loading: true,
    goalRecordsVisible: false,
    goalRecordsTitle: '',
    goalRecords: [],
    goalRecordsLoading: false
  },

  async onLoad() {
    try {
      var items = await planApi.getPlanItems();
      var cats = this._groupByCategory(items);
      this._categories = cats;
      this.setData({ categories: cats.map(function (c) { return c.name; }), loading: false });
      this.applyFilters();
    } catch (err) {
      this.setData({ loading: false });
      wx.showToast({ title: '加载训练计划失败', icon: 'none' });
    }
  },

  onShow() {
    if (!store.getTeacherId()) wx.reLaunch({ url: '/pages/login/login' });
  },

  _groupByCategory(items) {
    var map = {};
    var order = [];
    (items || []).forEach(function (item) {
      var catName = item.categoryLabel || '其他';
      if (!map[catName]) { map[catName] = { name: catName, items: [] }; order.push(catName); }
      map[catName].items.push({
        id: item.id,
        goalId: item.goalId,
        standardNumber: item.standardNumber,
        title: item.goalText,
        level1: item.initialLevel || '',
        levelNow: item.currentLevel || '',
        phase: String(item.phase || 1),
        status: STATUS_MAP[item.status] || '未开始'
      });
    });
    return order.map(function (k) { return map[k]; });
  },

  applyFilters() {
    var cat = this._categories[this.data.activeCategory];
    if (!cat) { this.setData({ filteredItems: [], totalCount: 0 }); return; }
    var kw = this.data.keyword.trim().toLowerCase();
    var items = cat.items;
    if (kw) items = items.filter(function (p) { return p.title.toLowerCase().includes(kw) || String(p.standardNumber || '').includes(kw); });
    if (this.data.activeStatus) items = items.filter(function (p) { return p.status === this.data.activeStatus; }.bind(this));
    this.setData({ filteredItems: items, totalCount: cat.items.length });
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
    clearTimeout(this._searchTimer);
    this._searchTimer = setTimeout(this.applyFilters.bind(this), 300);
  },

  onStatusTap(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.status });
    this.applyFilters();
  },

  onCategoryTap(e) {
    this.setData({ activeCategory: Number(e.currentTarget.dataset.index) });
    this.applyFilters();
  },

  onFieldChange(e) {
    var _this = this;
    var id = e.currentTarget.dataset.id;
    var field = e.currentTarget.dataset.field;
    var ranges = { levelNow: LEVELS, phase: PHASES, status: STATUS_LIST };
    var displayValue = ranges[field][Number(e.detail.value)];
    var cat = this._categories[this.data.activeCategory];
    var item = cat.items.find(function (p) { return p.id === id; });
    if (!item) return;
    item[field] = displayValue;
    this._lastFieldChange = Date.now();
    this.applyFilters();
    // 同步后端
    var patch = {};
    if (field === 'levelNow') patch.currentLevel = displayValue;
    else if (field === 'phase') patch.phase = parseInt(displayValue);
    else if (field === 'status') patch.status = STATUS_REV[displayValue] || 'NOT_STARTED';
    planApi.updatePlanItem(id, patch).catch(function () {});
  },

  async onGoalTap(e) {
    if (e.target.dataset.field) return;
    var standardNumber = e.currentTarget.dataset.standardNumber;
    var goalText = e.currentTarget.dataset.goalText;
    if (!standardNumber) { wx.showToast({ title: '该目标暂无编号', icon: 'none' }); return; }
    this.setData({
      goalRecordsVisible: true,
      goalRecordsTitle: '【' + standardNumber + '】' + goalText,
      goalRecords: [],
      goalRecordsLoading: true
    });
    try {
      var res = await planApi.getGoalRecords(standardNumber, 10);
      var records = Array.isArray(res) ? res : (res && res.records ? res.records : []);
      this.setData({ goalRecords: records });
    } catch (err) {
      this.setData({ goalRecords: [] });
    } finally {
      this.setData({ goalRecordsLoading: false });
    }
  },

  onGoalRecordsClose() {
    this.setData({ goalRecordsVisible: false });
  }
});
