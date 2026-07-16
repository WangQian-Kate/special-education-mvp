// mock/behaviors.js
// 行为字典（抽自原型 special-ed-assistan_v2.html），id 需与后端字典表对齐
// positive=true 为正向行为（计数按钮绿色）

module.exports = [
  { id: 1, name: '离开座位', count: 0, positive: false },
  { id: 2, name: '尖叫', count: 0, positive: false },
  { id: 3, name: '攻击行为', count: 0, positive: false },
  { id: 4, name: '拒绝任务', count: 0, positive: false },
  { id: 5, name: '举手回答', count: 0, positive: true },
  { id: 6, name: '自言自语', count: 0, positive: false },
  { id: 7, name: '自伤行为', count: 0, positive: false },
  { id: 8, name: '配合指令', count: 0, positive: true }
];
