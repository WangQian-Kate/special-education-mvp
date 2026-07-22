// components/behavior-module/behavior-module.js
// 行为模块：可折叠卡片，内部包含多个 behavior-row
Component({
  properties: {
    module: { type: Object, value: {} },
    // 是否展开
    expanded: { type: Boolean, value: true }
  },

  methods: {
    onToggle() {
      this.triggerEvent('toggle', { moduleName: this.properties.module.name });
    },

    // ---- 转发子组件事件 ----
    onStatusChange(e) { this.triggerEvent('statuschange', e.detail); },
    onStatusTap(e)    { this.triggerEvent('statustap', e.detail); },
    onNameTap(e)      { this.triggerEvent('nametap', e.detail); },
    onSubNameTap(e)   { this.triggerEvent('subnametap', e.detail); },
    onRowToggle(e)    { this.triggerEvent('rowtoggle', e.detail); }
  }
});
