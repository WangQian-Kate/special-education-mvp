// utils/constants.js
// 全局枚举收口：页面/组件一律从这里引用，禁止散落字符串

/** 影子老师白名单（阶段二伪登录用，ID 需与后端白名单一致） */
const SHADOW_TEACHERS = [
  { id: 't001', name: '张老师', school: 'XX市特殊教育学校', role: '特教教师' },
  { id: 't002', name: '王老师', school: 'XX市特殊教育学校', role: '康复师' },
  { id: 't003', name: '李老师', school: 'XX市随班就读试点小学', role: '班主任' }
];

/** 行为功能 */
const BEHAVIOR_FUNCTIONS = [
  { value: 'attention', label: '获得注意' },
  { value: 'item', label: '获得物品/活动' },
  { value: 'escape', label: '逃避/回避' },
  { value: 'sensory', label: '感觉刺激' }
];

/** 行为环节 */
const BEHAVIOR_PHASES = ['课前准备', '课堂教学', '课间活动', '午休', '放学', '个训课', '感统训练', '户外活动', '其他'];

/** 课程列表 */
const COURSES = ['语文', '数学', '英语', '体育', '音乐', '美术', '课间', '午餐', '午休', '自习', '其他'];

/** 环境列表 */
const ENVIRONMENTS = ['教室', '阶梯教室', '实验室', '机房', '食堂', '操场', '楼道', '校外'];

/** 持续时间预设（分钟） */
const DURATIONS = [1, 2, 3, 5, 10, 15, 20, 30];

module.exports = {
  SHADOW_TEACHERS,
  BEHAVIOR_FUNCTIONS,
  BEHAVIOR_PHASES,
  COURSES,
  ENVIRONMENTS,
  DURATIONS
};
