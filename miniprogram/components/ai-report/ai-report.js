// components/ai-report/ai-report.js
// SEAT 框架 AI 分析报告组件
Component({
  properties: {
    report: {
      type: Object,
      value: null
    }
  },

  data: {
    confidenceColor: '#9ca3af',
    confidenceText: '低',
    difficultyMap: {
      EASY: '简单',
      MEDIUM: '中等',
      HARD: '困难'
    }
  },

  observers: {
    'report.hypothesizedFunction.confidenceLevel': function (level) {
      var colorMap = { HIGH: '#22c55e', MEDIUM: '#f59e0b', LOW: '#ef4444' };
      var textMap = { HIGH: '高', MEDIUM: '中', LOW: '低' };
      this.setData({
        confidenceColor: colorMap[level] || '#9ca3af',
        confidenceText: textMap[level] || '未知'
      });
    }
  },

  methods: {
    formatPct: function (v) {
      if (typeof v !== 'number') return '0%';
      return Math.round(v * 100) + '%';
    }
  }
});
