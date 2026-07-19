// pages/plans/plans.js
// TAB2 训练计划（阶段二：mock 数据 + 搜索/筛选/行内编辑跑通交互；阶段三接后端接口）
const mockPlans = require('../../mock/plans');
const store = require('../../utils/store');

const STATUS_LIST = ['未开始', '进行中', '已完成'];
const LEVELS = ['A', 'B', 'C', 'D', 'E', 'F'];
const PHASES = ['1', '2', '3'];

Page({
  data: {
    categories: [],       // 分类名列表（tab 用）
    activeCategory: 0,
    statusList: STATUS_LIST,
    activeStatus: '',     // '' = 全部
    keyword: '',
    levels: LEVELS,
    phases: PHASES,
    filteredItems: [],    // 当前分类过滤后的条目
    totalCount: 0         // 当前分类总条数
  },

  onLoad() {
    // 深拷贝 mock，编辑不污染模块缓存
    this._categories = mockPlans.map((c) => ({ ...c, items: c.items.map((i) => ({ ...i })) }));
    this.setData({ categories: this._categories.map((c) => c.name) });
    this.applyFilters();
  },

  onShow() {
    if (!store.getTeacherId()) {
      wx.reLaunch({ url: '/pages/login/login' });
    }
  },

  /** 按 分类 + 状态 + 关键字 过滤并刷新列表 */
  applyFilters() {
    const cat = this._categories[this.data.activeCategory];
    const kw = this.data.keyword.trim().toLowerCase();
    let items = cat.items;
    if (kw) items = items.filter((p) => p.title.toLowerCase().includes(kw) || String(p.id).includes(kw));
    if (this.data.activeStatus) items = items.filter((p) => p.status === this.data.activeStatus);
    this.setData({ filteredItems: items, totalCount: cat.items.length });
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
    // 轻量防抖
    clearTimeout(this._searchTimer);
    this._searchTimer = setTimeout(() => this.applyFilters(), 300);
  },

  onStatusTap(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.status });
    this.applyFilters();
  },

  onCategoryTap(e) {
    this.setData({ activeCategory: Number(e.currentTarget.dataset.index) });
    this.applyFilters();
  },

  /** 行内编辑：picker 选完更新对应字段（阶段三：同步 PUT 到后端） */
  onFieldChange(e) {
    const { id, field } = e.currentTarget.dataset;
    const ranges = { levelNow: LEVELS, phase: PHASES, status: STATUS_LIST };
    const value = ranges[field][Number(e.detail.value)];
    const cat = this._categories[this.data.activeCategory];
    const item = cat.items.find((p) => p.id === id);
    if (!item) return;
    item[field] = value;
    this.applyFilters();
    // TODO 阶段三：api/plan.js 更新接口
  }
});
