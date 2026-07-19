// mock/plans.js
// 训练计划字典（脚本抽自原型 special-ed-assistan_v2.html 的 planCategories）
// level*: A-F 能力等级；phase: 1-3 阶段；status: 未开始|进行中|已完成|暂停
module.exports = [
  {
    name: '学校/班级意识',
    items: [
      {
        id: 1,
        title: '准确说出学校名称',
        level1: 'C',
        levelNow: 'E',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 2,
        title: '准确说出班级名称',
        level1: 'C',
        levelNow: 'E',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 3,
        title: '准确称呼语文老师',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 4,
        title: '准确称呼数学老师',
        level1: 'D',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 5,
        title: '准确称呼英语老师',
        level1: 'A',
        levelNow: 'C',
        phase: '1',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 6,
        title: '准确说出1位同学的名字',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 7,
        title: '准确说出2位同学的名字',
        level1: 'A',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 8,
        title: '准确说出3位同学的名字',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 9,
        title: '准确说出4位同学的名字',
        level1: 'A',
        levelNow: 'A',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 10,
        title: '准确说出5位同学的名字',
        level1: 'A',
        levelNow: 'A',
        phase: '1',
        status: '未开始',
        color: 'gray'
      }
    ]
  },
  {
    name: '入校常识',
    items: [
      {
        id: 11,
        title: '主动和熟人(老师)打招呼',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 12,
        title: '主动和熟人(同学)打招呼',
        level1: 'C',
        levelNow: 'C',
        phase: '1',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 13,
        title: '主动和熟人(门卫)打招呼',
        level1: 'B',
        levelNow: 'C',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 14,
        title: '主动和熟人(阿姨)打招呼',
        level1: 'B',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 15,
        title: '洗手',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 16,
        title: '排队',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 17,
        title: '配合晨检',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 18,
        title: '找到自己的班级',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 19,
        title: '调整桌椅间距',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 20,
        title: '放水壶',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 21,
        title: '脱外衣',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 22,
        title: '按照老师要求安静的等待上课',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      }
    ]
  },
  {
    name: '离校常规',
    items: [
      {
        id: 23,
        title: '穿外套',
        level1: 'D',
        levelNow: 'E',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 24,
        title: '拿水壶',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 25,
        title: '整理书包',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 26,
        title: '主动老师说再见',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 27,
        title: '主动和同学说再见',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 28,
        title: '主动和门卫、阿姨说再见',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 29,
        title: '跟随认识的接送成人走',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 30,
        title: '跟随老师指引上校车',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 31,
        title: '离校时安静',
        level1: 'C',
        levelNow: 'E',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 32,
        title: '离校时有秩序',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      }
    ]
  },
  {
    name: '运动',
    items: [
      {
        id: 33,
        title: '听从集合，并作出相应动作',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 34,
        title: '听从向×看，并作出相应动作',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 35,
        title: '听从向×转，并作出相应动作',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 36,
        title: '听从解散队列指令',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 37,
        title: '遵守体育课相关的常规规则',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 38,
        title: '及时关注活动结束的信号(音乐)',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 39,
        title: '关注活动结束的信号并参与收尾',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 40,
        title: '完成体育老师要求的运动项目',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 41,
        title: '运动时主动遵守游戏规则-轮流',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 42,
        title: '运动时主动遵守游戏规则-等待',
        level1: 'B',
        levelNow: 'C',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 43,
        title: '运动时有一定的安全意识',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 44,
        title: '运动时能辨别危险',
        level1: 'A',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 45,
        title: '运动时能躲避危险',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 46,
        title: '主动与他人进行合作游戏',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 47,
        title: '在他人发起时进行合作游戏',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '已完成',
        color: 'green'
      },
      {
        id: 48,
        title: '能理解集体性游戏的规则',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 49,
        title: '参与集体性规则运动-接力赛跑',
        level1: 'A',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 50,
        title: '参与集体性规则运动-热身准备',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 51,
        title: '有情况知道报告老师-表达需求',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 52,
        title: '有情况知道报告老师-告状',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 53,
        title: '知道根据身体状况自主喝水',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 54,
        title: '知道根据身体状况增减衣物',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'orange'
      }
    ]
  },
  {
    name: '做操',
    items: [
      {
        id: 55,
        title: '按要求准确地站在自己的位置上',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 56,
        title: '能模仿领操同学完成规定动作',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 57,
        title: '与同伴互动做操-变化队形',
        level1: 'A',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 58,
        title: '与同伴互动做操-拉手围圈',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 59,
        title: '做操时控制好自己的动作',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 60,
        title: '做操时不误伤他人',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 61,
        title: '做操时保持安静',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 62,
        title: '做操时不推操同伴',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 63,
        title: '两操之间耐心等待老师安排',
        level1: 'B',
        levelNow: 'C',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 64,
        title: '两操之间坚持排在队伍中',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      }
    ]
  },
  {
    name: '上下楼梯',
    items: [
      {
        id: 65,
        title: '上下楼梯动作稳定',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 66,
        title: '上下楼梯动作协调',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 67,
        title: '不在楼梯上打闹',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 68,
        title: '不在楼梯上玩游戏',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 69,
        title: '不在楼梯上做危险动作',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 70,
        title: '排队上下楼梯时跟紧同伴',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 71,
        title: '排队上下楼梯时不掉队',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 72,
        title: '上下楼梯时保持安静',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 73,
        title: '上下楼梯时不推操同伴',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 74,
        title: '遵守上下楼梯靠右走的规则',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 75,
        title: '根据临时情况调整行进节奏',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      }
    ]
  },
  {
    name: '排队',
    items: [
      {
        id: 76,
        title: '遵守班级的排队规则',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 77,
        title: '听到排队指令后快速在指定位置排队',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 78,
        title: '独立根据自己的位置报数',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 79,
        title: '排队时不推搡',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 80,
        title: '排队时远离同伴',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 81,
        title: '排队等待时不与同伴嬉戏打闹',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 82,
        title: '排队等待时不与同伴大声聊天',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 83,
        title: '坚持排在队伍中等待下一步指令',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 84,
        title: '能自主跟随排队队伍回班级',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 85,
        title: '能自主跟随排队队伍回班级(不走混)',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 86,
        title: '排队行走时能跟紧前一个人',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 87,
        title: '排队行走时不掉队',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      }
    ]
  },
  {
    name: '用餐',
    items: [
      {
        id: 88,
        title: '准确拿出餐盒和餐具',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 89,
        title: '按时拿出餐盒和餐具',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 90,
        title: '筷子使用姿势正确',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 91,
        title: '勺子使用姿势正确',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 92,
        title: '能较灵活地双手协调吃饭',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 93,
        title: '吃饭习惯良好-坐端正',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 94,
        title: '吃饭习惯良好-专注食物',
        level1: 'B',
        levelNow: 'C',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 95,
        title: '安静地坐在座位上吃饭',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 96,
        title: '按需求添加食物',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 97,
        title: '不破坏食物',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 98,
        title: '不浪费食物',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 99,
        title: '在规定时间内吃完食物',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 100,
        title: '吃饱后及时停止进餐',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 101,
        title: '吃完后将餐盒放到指定位置',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 102,
        title: '收拾餐具',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 103,
        title: '整理清洁自己的桌椅',
        level1: 'B',
        levelNow: 'C',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 104,
        title: '拿餐盒时动作轻柔不打翻',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 105,
        title: '收饭盒时动作轻柔不打翻',
        level1: 'C',
        levelNow: 'E',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 106,
        title: '保持衣物整洁',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 107,
        title: '熟练清理桌面',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 108,
        title: '准确清理桌面',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 109,
        title: '熟练清理地面污渍',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 110,
        title: '准确清理地面污渍',
        level1: 'A',
        levelNow: 'A',
        phase: '1',
        status: '未开始',
        color: 'gray'
      }
    ]
  },
  {
    name: '课间休息',
    items: [
      {
        id: 111,
        title: '课间根据生理需求小便',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 112,
        title: '课间根据生理需求大便',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 113,
        title: '独立整理好衣物再出来',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 114,
        title: '独立完成洗手程序',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 115,
        title: '保持手部干净-脏了及时洗手',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 116,
        title: '保持手部干净-脏了用纸巾擦手',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 117,
        title: '保持手部干净-脏了用湿纸巾擦手',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 118,
        title: '保持衣物整洁-脏了更换',
        level1: 'B',
        levelNow: 'C',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 119,
        title: '保持衣物整洁-脏了清洗',
        level1: 'A',
        levelNow: 'A',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 120,
        title: '衣服叠好放在指定的位置',
        level1: 'B',
        levelNow: 'C',
        phase: '2',
        status: '进行中',
        color: 'blue'
      }
    ]
  },
  {
    name: '集体课',
    items: [
      {
        id: 131,
        title: '坐姿端正正面朝老师',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 132,
        title: '认真观看老师播放的视频',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 133,
        title: '认真观看老师播放的课件',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 134,
        title: '认真观看老师播放的图片',
        level1: 'D',
        levelNow: 'F',
        phase: '3',
        status: '已完成',
        color: 'green'
      },
      {
        id: 135,
        title: '关注老师的讲解',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 136,
        title: '关注老师示范',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 137,
        title: '主动关注老师的简单提问',
        level1: 'B',
        levelNow: 'C',
        phase: '2',
        status: '未开始',
        color: 'orange'
      },
      {
        id: 138,
        title: '主动回应老师的简单提问',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 139,
        title: '主动关注老师的复杂提问',
        level1: 'A',
        levelNow: 'B',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 140,
        title: '主动回应老师的复杂提问',
        level1: 'A',
        levelNow: 'A',
        phase: '1',
        status: '未开始',
        color: 'gray'
      },
      {
        id: 145,
        title: '主动用合适的方式举手',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 146,
        title: '主动用合适的方式眼睛看向老师',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 150,
        title: '按要求跟读',
        level1: 'C',
        levelNow: 'E',
        phase: '3',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 151,
        title: '按要求跟唱',
        level1: 'B',
        levelNow: 'D',
        phase: '2',
        status: '进行中',
        color: 'blue'
      },
      {
        id: 152,
        title: '按要求做动作',
        level1: 'C',
        levelNow: 'D',
        phase: '2',
        status: '未开始',
        color: 'orange'
      }
    ]
  }
];
