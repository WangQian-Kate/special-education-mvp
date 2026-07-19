// utils/constants.js
// 全局枚举收口：页面/组件一律从这里引用，禁止散落字符串
// 编码以业务需求为准，与后端不同步的部分待后端后续更新字典表

/** 影子老师白名单（阶段二伪登录用，ID 需与后端白名单一致） */
const SHADOW_TEACHERS = [
  { id: 't001', name: '张老师', school: 'XX市特殊教育学校', role: '特教教师' },
  { id: 't002', name: '王老师', school: 'XX市特殊教育学校', role: '康复师' },
  { id: 't003', name: '李老师', school: 'XX市随班就读试点小学', role: '班主任' }
];

/** 课程列表（18 门；全天汇总为前端特殊模式，无后端接口，仅做统计展示+六维评价） */
const COURSES = [
  { code: 'CHINESE', label: '语文' },
  { code: 'MATHEMATICS', label: '数学' },
  { code: 'ENGLISH', label: '英语' },
  { code: 'BREAK', label: '课间' },
  { code: 'MUSIC', label: '音乐' },
  { code: 'PHYSICAL_EDUCATION', label: '体育' },
  { code: 'ART', label: '美术' },
  { code: 'DAOFA', label: '道法' },
  { code: 'LABOR', label: '劳动' },
  { code: 'COMPREHENSIVE', label: '综合' },
  { code: 'SCIENCE', label: '科学' },
  { code: 'LOCAL', label: '地方' },
  { code: 'LUNCH', label: '午餐' },
  { code: 'NOON_REST', label: '午休' },
  { code: 'SELF_STUDY', label: '自习' },
  { code: 'OTHER', label: '其他' },
  { code: 'ALL_DAY_SUMMARY', label: '全天汇总' }
];

/** 全天汇总特殊课程编码 */
const ALL_DAY_COURSE = 'ALL_DAY_SUMMARY';

/** 环境列表（全天汇总专用 "—" 项） */
const ENVIRONMENTS = [
  { code: 'CLASSROOM', label: '教室' },
  { code: 'PLAYGROUND', label: '操场' },
  { code: 'FUNCTION_ROOM', label: '功能教室' },
  { code: 'MUSIC_ROOM', label: '音乐教室' },
  { code: 'ART_ROOM', label: '美术教室' },
  { code: 'COMPUTER_ROOM', label: '机房' },
  { code: 'CAFETERIA', label: '食堂' },
  { code: 'SCHOOL_BUS', label: '校车' },
  { code: 'OTHER', label: '其他' },
  { code: 'NONE', label: '—' }
];

/** 课程 → 自动默认环境；未列出取 CLASSROOM */
const COURSE_DEFAULT_ENV = {
  PHYSICAL_EDUCATION: 'PLAYGROUND',
  BREAK: 'PLAYGROUND',
  LUNCH: 'CAFETERIA',
  ALL_DAY_SUMMARY: 'NONE'
};

/** 辅助方式（9 项分两组；勾选不强制填内容，保存时只传 {code}） */
const ASSISTANCE_GROUPS = [
  {
    group: '刺激内辅助',
    items: [
      { code: 'ADD_EXTERNAL_ITEM', label: '增加外在物品' },
      { code: 'CHANGE_TARGET_SIZE', label: '改变目标物大小' }
    ]
  },
  {
    group: '刺激外辅助',
    items: [
      { code: 'FULL_PHYSICAL', label: '全身体辅助' },
      { code: 'PARTIAL_PHYSICAL', label: '半身辅助' },
      { code: 'POSTURAL', label: '姿势辅助' },
      { code: 'POSITIONAL', label: '位置辅助' },
      { code: 'VERBAL', label: '语言辅助' },
      { code: 'DEMONSTRATION', label: '示范辅助' },
      { code: 'VISUAL', label: '视觉辅助' }
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
  { code: 'ATTENTION', label: '获得注意' },
  { code: 'TANGIBLE', label: '获得物品/活动' },
  { code: 'ESCAPE', label: '逃避/回避' },
  { code: 'SENSORY', label: '感觉刺激' }
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
