USE special_ed_assistant;

INSERT INTO app_user (id, avatar, name, school, position, role) VALUES
  (1, NULL, '张老师', 'XX市特殊教育学校', '资源教师', 'RESOURCE_TEACHER')
AS new
ON DUPLICATE KEY UPDATE
  avatar = new.avatar,
  name = new.name,
  school = new.school,
  position = new.position,
  role = new.role;

INSERT INTO student (
  id, name, age, class_name, disability_type, support_goal, remark
) VALUES
  (1, '小明', 12, '六年级一班', '自闭症谱系障碍', '提升课堂任务持续参与能力', '仅用于本地开发')
AS new
ON DUPLICATE KEY UPDATE
  name = new.name,
  age = new.age,
  class_name = new.class_name,
  disability_type = new.disability_type,
  support_goal = new.support_goal,
  remark = new.remark;

INSERT INTO app_user_student (user_id, student_id) VALUES
  (1, 1)
AS new
ON DUPLICATE KEY UPDATE student_id = new.student_id;

INSERT INTO app_user_current_student (user_id, student_id) VALUES
  (1, 1)
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
  ('VISUAL_PROMPT', '视觉提示', 'INTERNAL_STIMULUS', 1),
  ('VERBAL_PROMPT', '语言提示', 'INTERNAL_STIMULUS', 2),
  ('ACTION_PROMPT', '动作提示', 'INTERNAL_STIMULUS', 3),
  ('DEMONSTRATION', '示范', 'EXTERNAL_STIMULUS', 4),
  ('PHYSICAL_ASSISTANCE', '身体辅助', 'EXTERNAL_STIMULUS', 5),
  ('REINFORCEMENT', '强化', 'EXTERNAL_STIMULUS', 6),
  ('ASSISTIVE_DEVICE', '辅助设备', 'EXTERNAL_STIMULUS', 7)
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
  ('CUSTOM', '自定义', 999, TRUE)
AS new
ON DUPLICATE KEY UPDATE
  label = new.label,
  display_order = new.display_order,
  is_custom = new.is_custom;

-- 行为环节、行为功能、163 项标准训练目标及等级数据尚未提供，暂不写入虚假数据。
