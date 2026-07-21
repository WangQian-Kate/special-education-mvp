USE special_ed_assistant;

INSERT INTO app_user (id, teacher_id, avatar, name, school, position, role) VALUES
  (1, 't001', NULL, '张老师', 'XX市特殊教育学校', '特教教师', 'RESOURCE_TEACHER'),
  (2, 't002', NULL, '王老师', 'XX市特殊教育学校', '康复师', 'RESOURCE_TEACHER'),
  (3, 't003', NULL, '李老师', 'XX市随班就读试点小学', '班主任', 'RESOURCE_TEACHER')
AS new
ON DUPLICATE KEY UPDATE
  teacher_id = new.teacher_id,
  avatar = new.avatar,
  name = new.name,
  school = new.school,
  position = new.position,
  role = new.role;

INSERT INTO student (
  id, student_code, name, gender, age, class_name, disability_type, support_goal, remark
) VALUES
  (1, 's001', '小明', 'MALE', 8, '二年级1班', '自闭症谱系障碍', '提升课堂任务持续参与能力', '仅用于本地开发'),
  (2, 's002', '小华', 'MALE', 9, '三年级2班', NULL, NULL, '仅用于本地开发'),
  (3, 's003', '小红', 'FEMALE', 7, '一年级1班', NULL, NULL, '仅用于本地开发'),
  (4, 's004', '小丽', 'FEMALE', 9, '三年级1班', NULL, NULL, '仅用于本地开发'),
  (5, 's005', '小刚', 'MALE', 10, '四年级2班', NULL, NULL, '仅用于本地开发'),
  (6, 's006', '小强', 'MALE', 8, '二年级3班', NULL, NULL, '仅用于本地开发')
AS new
ON DUPLICATE KEY UPDATE
  student_code = new.student_code,
  name = new.name,
  gender = new.gender,
  age = new.age,
  class_name = new.class_name,
  disability_type = new.disability_type,
  support_goal = new.support_goal,
  remark = new.remark;

INSERT INTO app_user_student (user_id, student_id) VALUES
  (1, 1),
  (1, 2),
  (2, 3),
  (2, 4),
  (3, 5),
  (3, 6)
AS new
ON DUPLICATE KEY UPDATE student_id = new.student_id;

INSERT INTO app_user_current_student (user_id, student_id) VALUES
  (1, 1),
  (2, 3),
  (3, 5)
AS new
ON DUPLICATE KEY UPDATE student_id = new.student_id;

INSERT INTO course_type (code, label) VALUES
  ('CHINESE', '语文'),
  ('MATHEMATICS', '数学'),
  ('ENGLISH', '英语'),
  ('PHYSICAL_EDUCATION', '体育'),
  ('MUSIC', '音乐'),
  ('ART', '美术'),
  ('BREAK', '课间'),
  ('LUNCH', '午餐'),
  ('NOON_REST', '午休'),
  ('SELF_STUDY', '自习'),
  ('OTHER', '其他')
AS new
ON DUPLICATE KEY UPDATE label = new.label;

INSERT INTO environment_type (code, label) VALUES
  ('CLASSROOM', '教室'),
  ('PLAYGROUND', '操场'),
  ('FUNCTION_ROOM', '功能教室'),
  ('MUSIC_ROOM', '音乐教室'),
  ('ART_ROOM', '美术教室'),
  ('COMPUTER_ROOM', '机房'),
  ('CAFETERIA', '食堂'),
  ('SCHOOL_BUS', '校车'),
  ('OTHER', '其他')
AS new
ON DUPLICATE KEY UPDATE label = new.label;

INSERT INTO behavior_type (code, label) VALUES
  ('LEAVE_SEAT', '离开座位'),
  ('ATTENTION_DROP', '注意力下降'),
  ('RAISE_HAND', '举手'),
  ('ANSWER_QUESTION', '回答问题'),
  ('RAISE_HAND_ANSWER', '举手回答'),
  ('SCREAM', '尖叫'),
  ('CRY', '哭闹'),
  ('AGGRESSION', '攻击行为'),
  ('TASK_REFUSAL', '拒绝任务'),
  ('SELF_TALK', '自言自语'),
  ('SELF_INJURY', '自伤行为'),
  ('FOLLOW_INSTRUCTION', '配合指令'),
  ('QUEUE', '排队'),
  ('RUN', '奔跑'),
  ('COOPERATION', '合作'),
  ('FOLLOW_RULES', '遵守规则')
AS new
ON DUPLICATE KEY UPDATE label = new.label;

INSERT INTO assistance_type (code, label, group_code, display_order) VALUES
  ('ADD_EXTERNAL_OBJECT', '增加外在物品', 'INTERNAL_STIMULUS', 1),
  ('CHANGE_TARGET_SIZE', '改变目标物大小', 'INTERNAL_STIMULUS', 2),
  ('FULL_BODY_ASSISTANCE', '全身体辅助', 'EXTERNAL_STIMULUS', 3),
  ('HALF_BODY_ASSISTANCE', '半身辅助', 'EXTERNAL_STIMULUS', 4),
  ('POSTURE_ASSISTANCE', '姿势辅助', 'EXTERNAL_STIMULUS', 5),
  ('POSITION_ASSISTANCE', '位置辅助', 'EXTERNAL_STIMULUS', 6),
  ('VERBAL_ASSISTANCE', '语言辅助', 'EXTERNAL_STIMULUS', 7),
  ('DEMONSTRATION_ASSISTANCE', '示范辅助', 'EXTERNAL_STIMULUS', 8),
  ('VISUAL_ASSISTANCE', '视觉辅助', 'EXTERNAL_STIMULUS', 9)
AS new
ON DUPLICATE KEY UPDATE
  label = new.label,
  group_code = new.group_code,
  display_order = new.display_order;

INSERT INTO course_behavior_config (
  course_code, behavior_code, configured_by_user_id
) VALUES
  ('CHINESE', 'LEAVE_SEAT', NULL),
  ('CHINESE', 'SCREAM', NULL),
  ('CHINESE', 'AGGRESSION', NULL),
  ('CHINESE', 'TASK_REFUSAL', NULL),
  ('CHINESE', 'RAISE_HAND_ANSWER', NULL),
  ('CHINESE', 'SELF_TALK', NULL),
  ('CHINESE', 'SELF_INJURY', NULL),
  ('CHINESE', 'FOLLOW_INSTRUCTION', NULL),
  ('PHYSICAL_EDUCATION', 'QUEUE', NULL),
  ('PHYSICAL_EDUCATION', 'RUN', NULL),
  ('PHYSICAL_EDUCATION', 'AGGRESSION', NULL),
  ('PHYSICAL_EDUCATION', 'COOPERATION', NULL),
  ('PHYSICAL_EDUCATION', 'FOLLOW_RULES', NULL)
AS new
ON DUPLICATE KEY UPDATE configured_by_user_id = new.configured_by_user_id;

INSERT INTO training_goal_category (
  code, label, display_order, is_custom
) VALUES
  ('SCHOOL_CLASS_AWARENESS', '学校/班级意识', 1, FALSE),
  ('SCHOOL_ENTRY_KNOWLEDGE', '入校常识', 2, FALSE),
  ('SCHOOL_LEAVING_ROUTINE', '离校常规', 3, FALSE),
  ('SPORTS', '运动', 4, FALSE),
  ('EXERCISES', '做操', 5, FALSE),
  ('STAIRS', '上下楼梯', 6, FALSE),
  ('QUEUE', '排队', 7, FALSE),
  ('DINING', '用餐', 8, FALSE),
  ('BREAK_TIME', '课间休息', 9, FALSE),
  ('GROUP_CLASS', '集体课', 10, FALSE),
  ('CUSTOM', '自定义', 999, TRUE)
AS new
ON DUPLICATE KEY UPDATE
  label = new.label,
  display_order = new.display_order,
  is_custom = new.is_custom;

INSERT INTO training_goal (
  standard_number, category_code, goal_text, goal_type, owner_student_id
) VALUES
  (1, 'SCHOOL_CLASS_AWARENESS', '准确说出学校名称', 'STANDARD', NULL),
  (2, 'SCHOOL_CLASS_AWARENESS', '准确说出班级名称', 'STANDARD', NULL),
  (3, 'SCHOOL_CLASS_AWARENESS', '准确称呼语文老师', 'STANDARD', NULL),
  (4, 'SCHOOL_CLASS_AWARENESS', '准确称呼数学老师', 'STANDARD', NULL),
  (5, 'SCHOOL_CLASS_AWARENESS', '准确称呼英语老师', 'STANDARD', NULL),
  (6, 'SCHOOL_CLASS_AWARENESS', '准确说出1位同学的名字', 'STANDARD', NULL),
  (7, 'SCHOOL_CLASS_AWARENESS', '准确说出2位同学的名字', 'STANDARD', NULL),
  (8, 'SCHOOL_CLASS_AWARENESS', '准确说出3位同学的名字', 'STANDARD', NULL),
  (9, 'SCHOOL_CLASS_AWARENESS', '准确说出4位同学的名字', 'STANDARD', NULL),
  (10, 'SCHOOL_CLASS_AWARENESS', '准确说出5位同学的名字', 'STANDARD', NULL),
  (11, 'SCHOOL_ENTRY_KNOWLEDGE', '主动和熟人(老师)打招呼', 'STANDARD', NULL),
  (12, 'SCHOOL_ENTRY_KNOWLEDGE', '主动和熟人(同学)打招呼', 'STANDARD', NULL),
  (13, 'SCHOOL_ENTRY_KNOWLEDGE', '主动和熟人(门卫)打招呼', 'STANDARD', NULL),
  (14, 'SCHOOL_ENTRY_KNOWLEDGE', '主动和熟人(阿姨)打招呼', 'STANDARD', NULL),
  (15, 'SCHOOL_ENTRY_KNOWLEDGE', '洗手', 'STANDARD', NULL),
  (16, 'SCHOOL_ENTRY_KNOWLEDGE', '排队', 'STANDARD', NULL),
  (17, 'SCHOOL_ENTRY_KNOWLEDGE', '配合晨检', 'STANDARD', NULL),
  (18, 'SCHOOL_ENTRY_KNOWLEDGE', '找到自己的班级', 'STANDARD', NULL),
  (19, 'SCHOOL_ENTRY_KNOWLEDGE', '调整桌椅间距', 'STANDARD', NULL),
  (20, 'SCHOOL_ENTRY_KNOWLEDGE', '放水壶', 'STANDARD', NULL),
  (21, 'SCHOOL_ENTRY_KNOWLEDGE', '脱外衣', 'STANDARD', NULL),
  (22, 'SCHOOL_ENTRY_KNOWLEDGE', '按照老师要求安静的等待上课', 'STANDARD', NULL),
  (23, 'SCHOOL_LEAVING_ROUTINE', '穿外套', 'STANDARD', NULL),
  (24, 'SCHOOL_LEAVING_ROUTINE', '拿水壶', 'STANDARD', NULL),
  (25, 'SCHOOL_LEAVING_ROUTINE', '整理书包', 'STANDARD', NULL),
  (26, 'SCHOOL_LEAVING_ROUTINE', '主动老师说再见', 'STANDARD', NULL),
  (27, 'SCHOOL_LEAVING_ROUTINE', '主动和同学说再见', 'STANDARD', NULL),
  (28, 'SCHOOL_LEAVING_ROUTINE', '主动和门卫、阿姨说再见', 'STANDARD', NULL),
  (29, 'SCHOOL_LEAVING_ROUTINE', '跟随认识的接送成人走', 'STANDARD', NULL),
  (30, 'SCHOOL_LEAVING_ROUTINE', '跟随老师指引上校车', 'STANDARD', NULL),
  (31, 'SCHOOL_LEAVING_ROUTINE', '离校时安静', 'STANDARD', NULL),
  (32, 'SCHOOL_LEAVING_ROUTINE', '离校时有秩序', 'STANDARD', NULL),
  (33, 'SPORTS', '听从集合，并作出相应动作', 'STANDARD', NULL),
  (34, 'SPORTS', '听从向×看，并作出相应动作', 'STANDARD', NULL),
  (35, 'SPORTS', '听从向×转，并作出相应动作', 'STANDARD', NULL),
  (36, 'SPORTS', '听从解散队列指令，并作出相应动作', 'STANDARD', NULL),
  (37, 'SPORTS', '遵守体育课相关的常规规则', 'STANDARD', NULL),
  (38, 'SPORTS', '及时关注活动结束的信号（音乐或老师提醒）', 'STANDARD', NULL),
  (39, 'SPORTS', '关注活动结束的信号（老师提醒）并参与收尾环节', 'STANDARD', NULL),
  (40, 'SPORTS', '完成体育老师要求的运动项目', 'STANDARD', NULL),
  (41, 'SPORTS', '运动时能主动遵守基本的游戏规则，如轮流', 'STANDARD', NULL),
  (42, 'SPORTS', '运动时能主动遵守基本的游戏规则，如等待', 'STANDARD', NULL),
  (43, 'SPORTS', '运动时有一定的安全意识', 'STANDARD', NULL),
  (44, 'SPORTS', '运动时能辨别危险', 'STANDARD', NULL),
  (45, 'SPORTS', '运动时能躲避危险', 'STANDARD', NULL),
  (46, 'SPORTS', '主动与他人进行合作游戏', 'STANDARD', NULL),
  (47, 'SPORTS', '在他人发起时，与他人进行合作游戏', 'STANDARD', NULL),
  (48, 'SPORTS', '能理解集体性游戏的规则', 'STANDARD', NULL),
  (49, 'SPORTS', '参与集体性规则运动，如“接力赛跑”', 'STANDARD', NULL),
  (50, 'SPORTS', '参与集体性规则运动，如“热身准备运动”', 'STANDARD', NULL),
  (51, 'SPORTS', '有情况知道报告老师（表达需求）', 'STANDARD', NULL),
  (52, 'SPORTS', '有情况知道报告老师（告状）', 'STANDARD', NULL),
  (53, 'SPORTS', '知道根据身体状况，自主喝水', 'STANDARD', NULL),
  (54, 'SPORTS', '知道根据身体状况，增减衣物', 'STANDARD', NULL),
  (55, 'EXERCISES', '按要求准确地站在自己的位置上', 'STANDARD', NULL),
  (56, 'EXERCISES', '能模仿领操同学完成规定动作', 'STANDARD', NULL),
  (57, 'EXERCISES', '与同伴互动做操，如变化队形', 'STANDARD', NULL),
  (58, 'EXERCISES', '与同伴互动做操，拉手围圈等', 'STANDARD', NULL),
  (59, 'EXERCISES', '做操时控制好自己的动作', 'STANDARD', NULL),
  (60, 'EXERCISES', '做操时不误伤他人', 'STANDARD', NULL),
  (61, 'EXERCISES', '做操时保持安静', 'STANDARD', NULL),
  (62, 'EXERCISES', '做操时不推操同伴', 'STANDARD', NULL),
  (63, 'EXERCISES', '两操之间和做完操时，耐心等待老师安排', 'STANDARD', NULL),
  (64, 'EXERCISES', '两操之间和做完操时，坚持排在队伍中', 'STANDARD', NULL),
  (65, 'STAIRS', '上下楼梯动作稳定', 'STANDARD', NULL),
  (66, 'STAIRS', '上下楼梯动作协调', 'STANDARD', NULL),
  (67, 'STAIRS', '有安全意识，不在楼梯上打闹', 'STANDARD', NULL),
  (68, 'STAIRS', '有安全意识，不在楼梯上玩游戏', 'STANDARD', NULL),
  (69, 'STAIRS', '有安全意识，不在楼梯上做危险的动作', 'STANDARD', NULL),
  (70, 'STAIRS', '排队上下楼梯时，跟紧同伴', 'STANDARD', NULL),
  (71, 'STAIRS', '排队上下楼梯时，不掉队', 'STANDARD', NULL),
  (72, 'STAIRS', '上下楼梯时保持安静', 'STANDARD', NULL),
  (73, 'STAIRS', '上下楼梯时不推操同伴', 'STANDARD', NULL),
  (74, 'STAIRS', '常规情况下，遵守上下楼梯靠右走的规则', 'STANDARD', NULL),
  (75, 'STAIRS', '根据临时情况（如其他班级经过）调整行进节奏', 'STANDARD', NULL),
  (76, 'QUEUE', '遵守班级的排队规则', 'STANDARD', NULL),
  (77, 'QUEUE', '听到排队指令后能快速在指定位置排队', 'STANDARD', NULL),
  (78, 'QUEUE', '独立根据自己的位置报数', 'STANDARD', NULL),
  (79, 'QUEUE', '排队时不推搡', 'STANDARD', NULL),
  (80, 'QUEUE', '排队时远离同伴', 'STANDARD', NULL),
  (81, 'QUEUE', '排队等待时不与同伴嬉戏打闹', 'STANDARD', NULL),
  (82, 'QUEUE', '排队等待时不与同伴大声聊天', 'STANDARD', NULL),
  (83, 'QUEUE', '坚持排在队伍中等待老师的下一步指令', 'STANDARD', NULL),
  (84, 'QUEUE', '能自主跟随排队队伍回班级（不会进入其它班级）', 'STANDARD', NULL),
  (85, 'QUEUE', '能自主跟随排队队伍回班级（走混）', 'STANDARD', NULL),
  (86, 'QUEUE', '排队行走时能跟紧前一个人', 'STANDARD', NULL),
  (87, 'QUEUE', '排队行走时不掉队', 'STANDARD', NULL),
  (88, 'DINING', '准确拿出餐盒和餐具', 'STANDARD', NULL),
  (89, 'DINING', '按时拿出餐盒和餐具', 'STANDARD', NULL),
  (90, 'DINING', '筷子使用姿势正确', 'STANDARD', NULL),
  (91, 'DINING', '勺子使用姿势正确', 'STANDARD', NULL),
  (92, 'DINING', '能较灵活地双手协调吃饭', 'STANDARD', NULL),
  (93, 'DINING', '吃饭习惯良好，坐端正', 'STANDARD', NULL),
  (94, 'DINING', '吃饭习惯良好，专注食物', 'STANDARD', NULL),
  (95, 'DINING', '安静地坐在座位上吃饭，尽量不说话', 'STANDARD', NULL),
  (96, 'DINING', '按需求添加食物', 'STANDARD', NULL),
  (97, 'DINING', '不破坏食物', 'STANDARD', NULL),
  (98, 'DINING', '不浪费食物', 'STANDARD', NULL),
  (99, 'DINING', '在规定时间内吃完食物', 'STANDARD', NULL),
  (100, 'DINING', '吃饱后及时停止进餐', 'STANDARD', NULL),
  (101, 'DINING', '吃完午餐后能将餐盒放到指定位置（或收拾到餐盒袋内）', 'STANDARD', NULL),
  (102, 'DINING', '收拾餐具', 'STANDARD', NULL),
  (103, 'DINING', '整理清洁自己的桌椅', 'STANDARD', NULL),
  (104, 'DINING', '拿餐盒时动作轻柔不打翻餐盒', 'STANDARD', NULL),
  (105, 'DINING', '收饭盒时动作轻柔不打翻饭盒', 'STANDARD', NULL),
  (106, 'DINING', '保持衣物整洁', 'STANDARD', NULL),
  (107, 'DINING', '熟练清理桌面', 'STANDARD', NULL),
  (108, 'DINING', '准确清理桌面', 'STANDARD', NULL),
  (109, 'DINING', '熟练清理地面污渍', 'STANDARD', NULL),
  (110, 'DINING', '准确清理地面污渍', 'STANDARD', NULL),
  (111, 'BREAK_TIME', '课间根据生理需求小便', 'STANDARD', NULL),
  (112, 'BREAK_TIME', '课间根据生理需求大便', 'STANDARD', NULL),
  (113, 'BREAK_TIME', '独立在厕所间，整理好衣物（如提好裤子等）再出来', 'STANDARD', NULL),
  (114, 'BREAK_TIME', '独立完成洗手程序（打湿手—用洗手液—搓手心手背—洗干净泡沫—擦干手）', 'STANDARD', NULL),
  (115, 'BREAK_TIME', '保持手部干净，脏了之后能及时洗手', 'STANDARD', NULL),
  (116, 'BREAK_TIME', '保持手部干净，脏了之后能用纸巾擦手', 'STANDARD', NULL),
  (117, 'BREAK_TIME', '保持手部干净，脏了之后能湿纸巾擦手', 'STANDARD', NULL),
  (118, 'BREAK_TIME', '保持衣物干净整洁，脏了后能根据情况更换', 'STANDARD', NULL),
  (119, 'BREAK_TIME', '保持衣物干净整洁，脏了后能根据情况清洗', 'STANDARD', NULL),
  (120, 'BREAK_TIME', '衣服叠好放在指定的位置', 'STANDARD', NULL),
  (121, 'BREAK_TIME', '去完厕所后回到自己班级', 'STANDARD', NULL),
  (122, 'BREAK_TIME', '去完厕所后回到自己座位', 'STANDARD', NULL),
  (123, 'BREAK_TIME', '在座位上无不恰当行为', 'STANDARD', NULL),
  (124, 'BREAK_TIME', '休息时自己看课外书', 'STANDARD', NULL),
  (125, 'BREAK_TIME', '休息时独自喝水', 'STANDARD', NULL),
  (126, 'BREAK_TIME', '休息时安静的坐在自己的座位上', 'STANDARD', NULL),
  (127, 'BREAK_TIME', '课间书本准备', 'STANDARD', NULL),
  (128, 'BREAK_TIME', '按老师指令检查座位周围卫生', 'STANDARD', NULL),
  (129, 'BREAK_TIME', '午休时按照老师要求进行', 'STANDARD', NULL),
  (130, 'BREAK_TIME', '午休结束后，准备上课用品', 'STANDARD', NULL),
  (131, 'GROUP_CLASS', '坐姿端正正面朝老师', 'STANDARD', NULL),
  (132, 'GROUP_CLASS', '认真观看老师播放的视频', 'STANDARD', NULL),
  (133, 'GROUP_CLASS', '认真观看老师播放的课件', 'STANDARD', NULL),
  (134, 'GROUP_CLASS', '认真观看老师播放的图片', 'STANDARD', NULL),
  (135, 'GROUP_CLASS', '关注老师的讲解', 'STANDARD', NULL),
  (136, 'GROUP_CLASS', '关注老师示范', 'STANDARD', NULL),
  (137, 'GROUP_CLASS', '主动关注老师的简单提问', 'STANDARD', NULL),
  (138, 'GROUP_CLASS', '主动回应老师的简单提问', 'STANDARD', NULL),
  (139, 'GROUP_CLASS', '主动关注老师的复杂提问', 'STANDARD', NULL),
  (140, 'GROUP_CLASS', '主动回应老师的复杂提问', 'STANDARD', NULL),
  (141, 'GROUP_CLASS', '主动关注课堂提问简单的问题', 'STANDARD', NULL),
  (142, 'GROUP_CLASS', '主动向老师提问简单的问题', 'STANDARD', NULL),
  (143, 'GROUP_CLASS', '主动关注课堂提问复杂的问题', 'STANDARD', NULL),
  (144, 'GROUP_CLASS', '主动向老师提问复杂的问题', 'STANDARD', NULL),
  (145, 'GROUP_CLASS', '主动用合适的方式举手', 'STANDARD', NULL),
  (146, 'GROUP_CLASS', '主动用合适的方式眼睛看向老师方向', 'STANDARD', NULL),
  (147, 'GROUP_CLASS', '主动用合适的方式回应老师的提问', 'STANDARD', NULL),
  (148, 'GROUP_CLASS', '主动关注同学的表达', 'STANDARD', NULL),
  (149, 'GROUP_CLASS', '主动关注其他同学的活动，判断是否正确', 'STANDARD', NULL),
  (150, 'GROUP_CLASS', '按要求跟读', 'STANDARD', NULL),
  (151, 'GROUP_CLASS', '按要求跟唱', 'STANDARD', NULL),
  (152, 'GROUP_CLASS', '按要求做动作', 'STANDARD', NULL),
  (153, 'GROUP_CLASS', '主动创作，创作内容与要求主体一致', 'STANDARD', NULL),
  (154, 'GROUP_CLASS', '展示自己的作品', 'STANDARD', NULL),
  (155, 'GROUP_CLASS', '介绍自己的作品', 'STANDARD', NULL),
  (156, 'GROUP_CLASS', '展示作品想法', 'STANDARD', NULL),
  (157, 'GROUP_CLASS', '安静的聆听同伴展示作品', 'STANDARD', NULL),
  (158, 'GROUP_CLASS', '安静地聆听同伴介绍自己作品的想法', 'STANDARD', NULL),
  (159, 'GROUP_CLASS', '听完他人介绍后可以给予恰当的评价', 'STANDARD', NULL),
  (160, 'GROUP_CLASS', '主动配合老师整理课堂用具', 'STANDARD', NULL),
  (161, 'GROUP_CLASS', '主动配合小组长整理课堂用具等', 'STANDARD', NULL),
  (162, 'GROUP_CLASS', '按要求收拾文具', 'STANDARD', NULL),
  (163, 'GROUP_CLASS', '按要求摆桌子', 'STANDARD', NULL),
  (164, 'GROUP_CLASS', '按要求推进椅子', 'STANDARD', NULL),
  (165, 'GROUP_CLASS', '按要求排队离开教室', 'STANDARD', NULL)
AS new
ON DUPLICATE KEY UPDATE
  category_code = new.category_code,
  goal_text = new.goal_text,
  goal_type = new.goal_type,
  owner_student_id = new.owner_student_id;

INSERT IGNORE INTO schema_migration (version) VALUES ('2.1.0');
INSERT IGNORE INTO schema_migration (version) VALUES ('2.2.0');
INSERT IGNORE INTO schema_migration (version) VALUES ('2.3.0');

-- 行为环节、行为功能和训练等级数据尚未提供，暂不写入虚假数据。
