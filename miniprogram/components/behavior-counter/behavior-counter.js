// components/behavior-counter/behavior-counter.js
const store = require('../../utils/store');
Component({
  properties: {
    behavior: {
      type: Object,
      value: { behaviorCode: '', behaviorLabel: '', count: 0, positive: false, pending: false }
    },
    classRecordId: { type: Number, value: 0 }
  },

  data: { bump: false },

  methods: {
    onMinus() {
      const b = this.properties.behavior;
      if (b.pending || b.count <= 0) return;
      if (!b.latestRecordId) return;
      const that = this;
      // 有详细记录先弹确认
      if (b.latestDetailSaved) {
        wx.showModal({
          title: '确认删除',
          content: '最近一次记录已填写详细信息，删除后不可恢复，确定删除吗？',
          confirmColor: '#f87171',
          success(res) {
            if (res.confirm) that._doDelete(b.latestRecordId);
          }
        });
      } else {
        this._doDelete(b.latestRecordId);
      }
    },

    _doDelete(rid) {
      const b = this.properties.behavior;
      this.triggerEvent('countchange', { behaviorCode: b.behaviorCode, delta: -1 });
      // 直接 wx.request 删除，不走任何封装
      wx.request({
        url: 'http://localhost:3001/api/behavior-records/' + rid,
        method: 'DELETE',
        header: { 'Content-Type': 'application/json', 'X-Teacher-Id': store.getTeacherId() },
        success: (r) => {
          const body = r.data || {};
          if (body.code === 0) {
            wx.showToast({ title: '已删除', icon: 'success' });
            this.triggerEvent('countchange', { behaviorCode: b.behaviorCode, delta: 0 }); // 通知页面刷新
          } else {
            wx.showToast({ title: body.message || '删除失败', icon: 'none' });
          }
        },
        fail: () => wx.showToast({ title: '网络异常', icon: 'none' })
      });
    },

    onPlus() {
      const b = this.properties.behavior;
      if (b.pending) return;
      this.setData({ bump: true });
      setTimeout(() => this.setData({ bump: false }), 200);
      this.triggerEvent('countchange', { behaviorCode: b.behaviorCode, delta: 1 });
    },

    onCardTap() {
      this.triggerEvent('opendetail', { behavior: this.properties.behavior });
    }
  }
});
