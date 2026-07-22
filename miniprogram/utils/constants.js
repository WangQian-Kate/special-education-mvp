// utils/constants.js
// 全局枚举收口：页面/组件一律从这里引用，禁止散落字符串
// 编码以业务需求为准，与后端不同步的部分待后端后续更新字典表

/** 影子老师白名单（阶段二伪登录用，ID 需与后端白名单一致） */
const SHADOW_TEACHERS = [
  { id: 't001', name: '张老师', school: 'XX市特殊教育学校', role: '特教教师' },
  { id: 't002', name: '王老师', school: 'XX市特殊教育学校', role: '康复师' },
  { id: 't003', name: '李老师', school: 'XX市随班就读试点小学', role: '班主任' }
];

/** 课程列表（16 门；全天汇总为前端特殊模式） */
const COURSES = [
  { code: 'ALL_DAY_SUMMARY', label: '全天汇总' },
  { code: 'CHINESE', label: '语文' },
  { code: 'MATHEMATICS', label: '数学' },
  { code: 'ENGLISH', label: '英语' },
  { code: 'PHYSICAL_EDUCATION', label: '体育' },
  { code: 'MUSIC', label: '音乐' },
  { code: 'ART', label: '美术' },
  { code: 'SCIENCE', label: '科学' },
  { code: 'MORAL_EDUCATION', label: '道法' },
  { code: 'PHYSICAL_TRAINING', label: '体能' },
  { code: 'INDIVIDUAL_TRAINING', label: '个训' },
  { code: 'BREAK', label: '课间' },
  { code: 'LUNCH', label: '午餐' },
  { code: 'NOON_REST', label: '午休' },
  { code: 'SELF_STUDY', label: '自习' },
  { code: 'OTHER', label: '其它' }
];

/** 全天汇总特殊课程编码 */
const ALL_DAY_COURSE = 'ALL_DAY_SUMMARY';

/** 环境列表（7 项） */
const ENVIRONMENTS = [
  { code: 'CLASSROOM', label: '普通教室' },
  { code: 'RESOURCE_CLASSROOM', label: '资源教室' },
  { code: 'PLAYGROUND', label: '操场' },
  { code: 'CORRIDOR', label: '楼道' },
  { code: 'RESTROOM', label: '卫生间' },
  { code: 'OFF_CAMPUS', label: '校外' },
  { code: 'OTHER', label: '其它' }
];

/** 课程 → 自动默认环境；未列出取 CLASSROOM */
const COURSE_DEFAULT_ENV = {
  PHYSICAL_EDUCATION: 'PLAYGROUND',
  ALL_DAY_SUMMARY: 'OTHER'
};

/** 辅助方式（9 项分两组；勾选不强制填内容，保存时只传 {code}） */
const ASSISTANCE_GROUPS = [
  {
    group: '刺激内辅助',
    items: [
      { code: 'ADD_EXTERNAL_OBJECT', label: '增加外在物品' },
      { code: 'CHANGE_TARGET_SIZE', label: '改变目标物大小' }
    ]
  },
  {
    group: '刺激外辅助',
    items: [
      { code: 'FULL_BODY_ASSISTANCE', label: '全身体辅助' },
      { code: 'HALF_BODY_ASSISTANCE', label: '半身辅助' },
      { code: 'POSTURE_ASSISTANCE', label: '姿势辅助' },
      { code: 'POSITION_ASSISTANCE', label: '位置辅助' },
      { code: 'VERBAL_ASSISTANCE', label: '语言辅助' },
      { code: 'DEMONSTRATION_ASSISTANCE', label: '示范辅助' },
      { code: 'VISUAL_ASSISTANCE', label: '视觉辅助' }
    ]
  }
];

/** 观察周期预设（分钟；value 为 'custom' 表示自定义输入） */
const DURATION_PRESETS = [
  { label: '15分钟', value: 15 },
  { label: '40分钟', value: 40 },
  { label: '60分钟', value: 60 },
  { label: '自定义', value: 'custom' }
];

/** 课程 → 默认观察周期（分钟）；未列出的取 _default */
const DEFAULT_DURATION = { BREAK: 15, _default: 40 };

/** 观察周期上限（后端 @Max(1440)） */
const DURATION_MAX = 1440;

/** 正向行为编码（后端不区分正负向，前端本地维护，+ 号显示绿色） */
const POSITIVE_BEHAVIOR_CODES = ['RAISE_HAND_ANSWER', 'FOLLOW_INSTRUCTION', 'COOPERATION', 'FOLLOW_RULES', 'QUEUE'];

/** 行为环节字典（暂空，等业务方提供） */
const STAGES = [];

/** 行为功能（4 项，选填；后端表暂空，提交时若后端未就绪则传 null） */
const FUNCTIONS = [
  { code: 'ATTENTION', label: '获取关注' },
  { code: 'TANGIBLE', label: '获取实物' },
  { code: 'ESCAPE', label: '逃避' },
  { code: 'SENSORY', label: '感官刺激' }
];

module.exports = {
  SHADOW_TEACHERS,
  COURSES,
  ALL_DAY_COURSE,
  ENVIRONMENTS,
  COURSE_DEFAULT_ENV,
  ASSISTANCE_GROUPS,
  DURATION_PRESETS,
  DEFAULT_DURATION,
  DURATION_MAX,
  POSITIVE_BEHAVIOR_CODES,
  STAGES,
  FUNCTIONS
};
