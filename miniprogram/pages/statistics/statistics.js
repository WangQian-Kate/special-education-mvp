// pages/statistics/statistics.js
// TAB2 学生评估（阶段二：静态数据跑通 ECharts 真机渲染；阶段三接统计接口）
import * as echarts from '../../components/ec-canvas/echarts';

// 静态 Demo 数据（抽自原型日报"行为频次统计"）
const DEMO_BEHAVIOR_FREQ = [
  { name: '离座', count: 3, positive: false },
  { name: '尖叫', count: 1, positive: false },
  { name: '拒绝', count: 2, positive: false },
  { name: '举手', count: 5, positive: true },
  { name: '自言', count: 1, positive: false }
];

function initChart(canvas, width, height, dpr) {
  const chart = echarts.init(canvas, null, { width, height, devicePixelRatio: dpr });
  canvas.setChart(chart);

  chart.setOption({
    grid: { left: 40, right: 16, top: 24, bottom: 28 },
    xAxis: {
      type: 'category',
      data: DEMO_BEHAVIOR_FREQ.map((b) => b.name),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#9ca3af', fontSize: 10 }
    },
    series: [
      {
        type: 'bar',
        barWidth: 18,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          // 正向行为绿色，问题行为主色蓝（同原型）
          color: (p) => (DEMO_BEHAVIOR_FREQ[p.dataIndex].positive ? '#4ade80' : '#5B9BD5')
        },
        data: DEMO_BEHAVIOR_FREQ.map((b) => b.count)
      }
    ]
  });
  return chart;
}

Page({
  data: {
    ec: { onInit: initChart },
    // 概况（静态 Demo，阶段三换统计接口）
    summary: [
      { value: 4, label: '观察课程', highlight: true },
      { value: 12, label: '行为记录' },
      { value: 3, label: 'ABC记录' },
      { value: 1, label: '备注' }
    ],
    // AI 分析（静态 Demo，阶段四换大模型接口）
    aiCards: [
      { type: 'info', title: '行为变化', content: '近一周离座行为下降40%，建议继续保持当前干预策略。' },
      { type: 'warn', title: '关注提醒', content: '下午注意力下降明显，建议调整训练时间至上午。' },
      { type: 'success', title: '替代建议', content: '推荐继续强化举手回答行为，已建立良好替代行为。' }
    ]
  }
});
