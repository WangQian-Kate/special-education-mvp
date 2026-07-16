// components/ai-card/ai-card.js
// AI 分析卡片：type 决定配色（info=蓝/趋势, warn=橙/预警, success=绿/建议）
Component({
  properties: {
    type: {
      type: String,
      value: 'info' // info | warn | success
    },
    title: {
      type: String,
      value: ''
    },
    content: {
      type: String,
      value: ''
    }
  }
});
