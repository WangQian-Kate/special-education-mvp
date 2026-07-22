// components/status-bar/status-bar.js
// 固定状态表头：未完成 | 辅助完成 | 独立完成
Component({
  properties: {
    labels: {
      type: Array,
      value: ['未完成', '辅助完成', '独立完成']
    }
  }
});
