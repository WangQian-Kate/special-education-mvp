// components/behavior-row/behavior-row.js
Component({
  properties: {
    behavior: { type: Object, value: {} },
    readonly: { type: Boolean, value: false }
  },

  data: {
    _expanded: true
  },

  observers: {
    'behavior': function () {
      this.setData({ _expanded: true });
    }
  },

  methods: {
    onStatusChange(e) {
      var sub = e.currentTarget.dataset.sub || '';
      var subLabel = e.currentTarget.dataset.subLabel || sub;
      var status = e.currentTarget.dataset.status;
      var delta = Number(e.currentTarget.dataset.delta);
      this.triggerEvent('statuschange', {
        behaviorName: this.properties.behavior.name,
        subBehavior: sub, subLabel: subLabel, status: status, delta: delta
      });
    },

    onStatusTap(e) {
      var sub = e.currentTarget.dataset.sub || '';
      var subLabel = e.currentTarget.dataset.subLabel || sub;
      var status = e.currentTarget.dataset.status;
      var count = Number(e.currentTarget.dataset.count);
      if (!count) return;
      this.triggerEvent('statustap', {
        behaviorName: this.properties.behavior.name,
        subBehavior: sub, subLabel: subLabel, status: status, count: count
      });
    },

    onNameTap() {
      var b = this.properties.behavior;
      if (b.subBehaviors && b.subBehaviors.length) {
        this.setData({ _expanded: !this.data._expanded });
      } else {
        this.triggerEvent('nametap', { behaviorCode: b.code, behaviorName: b.name });
      }
    },

    onSubNameTap(e) {
      this.triggerEvent('subnametap', {
        behaviorCode: this.properties.behavior.code,
        behaviorName: this.properties.behavior.name,
        subBehavior: e.currentTarget.dataset.sub,
        subLabel: e.currentTarget.dataset.subLabel || e.currentTarget.dataset.sub,
        subBehaviorCode: e.currentTarget.dataset.subCode || ''
      });
    }
  }
});
