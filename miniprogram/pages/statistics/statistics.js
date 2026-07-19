// pages/statistics/statistics.js
// 学生评估页：日报/周报/月报/学期报告四个子视图（阶段三 MVP：静态 mock 数据）
import * as echarts from '../../components/ec-canvas/echarts';

// ==================== 日报 mock ====================
const DAILY_FREQ = [
  { name: '离座', count: 3, positive: false },
  { name: '尖叫', count: 1, positive: false },
  { name: '拒绝', count: 2, positive: false },
  { name: '举手', count: 5, positive: true },
  { name: '自言', count: 1, positive: false }
];

// ==================== 周报 mock ====================
const WEEKLY_TRENDS = [
  { name: '离开座位', count: 23, pct: 65, change: -12, color: '#f87171' },
  { name: '尖叫', count: 5, pct: 20, change: -30, color: '#fb923c' },
  { name: '攻击行为', count: 2, pct: 8, change: -50, color: '#ef4444' },
  { name: '拒绝任务', count: 8, pct: 28, change: 5, color: '#eab308' },
  { name: '举手回答', count: 36, pct: 85, change: 20, color: '#4ade80' },
  { name: '自言自语', count: 7, pct: 22, change: -15, color: '#a855f7' }
];

// ==================== 月报 mock ====================
const MONTHLY_DIMENSIONS = [
  { name: '情绪行为', pct: 72, change: 8 },
  { name: '社会适应能力', pct: 65, change: 12 },
  { name: '社会交往能力', pct: 58, change: 5 },
  { name: '自我行为管理', pct: 60, change: 3 },
  { name: '语言理解及表达', pct: 55, change: 10 },
  { name: '自身/共同专注力', pct: 48, change: -6 }
];

const MONTHLY_BEHAVIORS = [
  { name: '离开座位', count: 85, pct: 70, color: '#f87171' },
  { name: '举手回答', count: 156, pct: 95, color: '#4ade80' },
  { name: '配合指令', count: 98, pct: 80, color: '#22c55e' },
  { name: '拒绝任务', count: 28, pct: 30, color: '#eab308' }
];

// ==================== 学期 mock ====================
const SEMESTER_DIMENSIONS = [
  { name: '情绪行为', initialPct: 45, currentPct: 68, improvement: 23, color: '#3b82f6' },
  { name: '社会适应能力', initialPct: 40, currentPct: 62, improvement: 22, color: '#4ade80' },
  { name: '社会交往能力', initialPct: 35, currentPct: 55, improvement: 20, color: '#a855f7' },
  { name: '自我行为管理', initialPct: 38, currentPct: 58, improvement: 20, color: '#fb923c' },
  { name: '语言理解及表达', initialPct: 30, currentPct: 52, improvement: 22, color: '#eab308' },
  { name: '自身/共同专注力', initialPct: 32, currentPct: 46, improvement: 14, color: '#ef4444' }
];

const SEMESTER_GOALS = [
  { name: '学校/班级意识', pct: 78 }, { name: '入校常识', pct: 85 },
  { name: '运动', pct: 65 }, { name: '集体课', pct: 58 },
  { name: '用餐', pct: 90 }, { name: '课间休息', pct: 72 }
];

// ==================== ECharts 初始化（日报柱状图） ====================
function initChart(canvas, width, height, dpr) {
  try {
    const chart = echarts.init(canvas, null, { width, height, devicePixelRatio: dpr });
    canvas.setChart(chart);
    chart.setOption({
      grid: { left: 40, right: 16, top: 24, bottom: 28 },
      xAxis: {
        type: 'category', data: DAILY_FREQ.map((b) => b.name),
        axisLine: { lineStyle: { color: '#e5e7eb' } },
        axisTick: { show: false },
        axisLabel: { color: '#9ca3af', fontSize: 10 }
      },
      yAxis: {
        type: 'value', minInterval: 1,
        splitLine: { lineStyle: { color: '#f3f4f6' } },
        axisLabel: { color: '#9ca3af', fontSize: 10 }
      },
      series: [{
        type: 'bar', barWidth: 18,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (p) => (DAILY_FREQ[p.dataIndex].positive ? '#4ade80' : '#5B9BD5')
        },
        data: DAILY_FREQ.map((b) => b.count)
      }]
    });
    return chart;
  } catch (err) {
    return null;  // ECharts init failed, canvas will render empty
  }
}

Page({
  data: {
    ec: { onInit: initChart },
    view: 'daily',
    // 日报
    dailySummary: [
      { value: 4, label: '观察课程', highlight: true },
      { value: 12, label: '行为记录' },
      { value: 3, label: 'ABC记录' },
      { value: 1, label: '备注' }
    ],
    dailyAiCards: [
      { type: 'info', title: '行为变化', content: '近一周离座行为下降40%，建议继续保持当前干预策略。' },
      { type: 'warn', title: '关注提醒', content: '下午注意力下降明显，建议调整训练时间至上午。' },
      { type: 'success', title: '替代建议', content: '推荐继续强化举手回答行为，已建立良好替代行为。' }
    ],
    // 周报
    weeklySummary: [
      { value: 18, label: '观察课程', highlight: true },
      { value: 96, label: '行为记录' },
      { value: 35, label: 'ABC记录' },
      { value: 6, label: '备注' }
    ],
    weeklyTrends: WEEKLY_TRENDS,
    weeklyAiCards: [
      { type: 'info', title: '整体趋势', content: '本周正向行为（举手回答、配合指令）显著增加，问题行为整体呈下降趋势。' },
      { type: 'warn', title: '重点关注', content: '拒绝任务行为略有上升，主要集中在数学课和下午时段，建议调整任务难度。' },
      { type: 'success', title: '干预建议', content: '举手回答已建立良好替代行为模式，可逐步减少强化频次，转向自然强化。' }
    ],
    // 月报
    monthlySummary: [
      { value: 82, label: '观察课程', highlight: true },
      { value: 415, label: '行为记录' },
      { value: 132, label: 'ABC记录' },
      { value: 26, label: '备注' }
    ],
    monthlyDimensions: MONTHLY_DIMENSIONS,
    monthlyBehaviors: MONTHLY_BEHAVIORS,
    monthlyAiCards: [
      { type: 'info', title: '月度总评', content: '本月整体表现良好，五大评估维度中有四项较上月提升，正向行为持续增长。' },
      { type: 'warn', title: '风险预警', content: '专注力维度略有下降（↓6%），可能与本月课程难度增加有关，建议下月重点干预。' },
      { type: 'success', title: '阶段成果', content: '举手回答行为已达156次/月，较学期初增长3倍，建议进入下一阶段训练目标。' }
    ],
    // 学期报告
    semesterSummary: [
      { value: '4.5', label: '月均课程', highlight: true },
      { value: 1860, label: '总行为记录' },
      { value: 520, label: 'ABC记录' },
      { value: 98, label: '教师备注' }
    ],
    semesterDimensions: SEMESTER_DIMENSIONS,
    semesterGoals: SEMESTER_GOALS,
    semesterAiCards: [
      { type: 'info', title: '学期总评', content: '本学期六大能力维度均有显著提升，平均进步20%，干预策略整体有效。' },
      { type: 'warn', title: '薄弱环节', content: '专注力维度进步最小（+14%），集体课参与度仅58%，建议下学期重点加强此方向。' },
      { type: 'success', title: '下学期建议', content: '1) 增加感统训练频次以改善专注力；2) 引入同伴支持策略提升社交互动；3) 数学课采用差异化教学降低任务拒绝率。' }
    ]
  },

  onSwitchView(e) {
    this.setData({ view: e.currentTarget.dataset.view });
  }
});
