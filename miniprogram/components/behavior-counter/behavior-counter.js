// components/behavior-counter/behavior-counter.js
// 行为计数卡片：+/- 计数即时反馈，点击卡片本体触发 detail 事件（阶段三接 ABC 弹窗）
Component({
  properties: {
    behavior: {
      type: Object,
      value: { id: 0, name: '', count: 0, positive: false }
    }
  },

  data: {
    // 计数动画开关
    bump: false
  },

  methods: {
    onMinus() {
      this._emitChange(-1);
    },

    onPlus() {
      this._emitChange(1);
    },

    /** 点击卡片本体（非 +/- 按钮）→ 上抛给页面打开 ABC 详录 */
    onCardTap() {
      this.triggerEvent('detail', { behavior: this.properties.behavior });
    },

    _emitChange(delta) {
      const b = this.properties.behavior;
      if (b.count + delta < 0) return;
      // 计数放大动画
      this.setData({ bump: true });
      setTimeout(() => this.setData({ bump: false }), 200);
      this.triggerEvent('change', { id: b.id, delta });
    }
  }
});
