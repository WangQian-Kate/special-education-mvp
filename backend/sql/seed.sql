USE special_ed_assistant;

INSERT INTO app_user (id, avatar, name, school, position, role) VALUES
  (1, NULL, '演示教师', '演示学校', '资源教师', 'RESOURCE_TEACHER')
AS new
ON DUPLICATE KEY UPDATE
  avatar = new.avatar,
  name = new.name,
  school = new.school,
  position = new.position,
  role = new.role;

INSERT INTO student (id, name, age, class_name, disability_type, support_goal, remark) VALUES
  (1, '演示学生', 9, '三年级一班', '演示数据', '提升课堂任务持续参与能力', '仅用于本地开发')
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

INSERT INTO antecedent_type (code, label) VALUES
  ('TEACHER_QUESTION', '教师提问')
AS new
ON DUPLICATE KEY UPDATE label = new.label;

INSERT INTO behavior_type (code, label) VALUES
  ('LEAVE_SEAT', '离开座位'),
  ('ATTENTION_DROP', '注意力下降'),
  ('RAISE_HAND', '举手'),
  ('ANSWER_QUESTION', '回答问题'),
  ('SCREAM', '尖叫'),
  ('CRY', '哭闹'),
  ('QUEUE', '排队'),
  ('RUN', '奔跑'),
  ('AGGRESSION', '攻击行为'),
  ('COOPERATION', '合作'),
  ('FOLLOW_RULES', '遵守规则'),
  ('TASK_REFUSAL', '拒绝任务')
AS new
ON DUPLICATE KEY UPDATE label = new.label;

INSERT INTO consequence_type (code, label) VALUES
  ('VERBAL_PROMPT', '教师提醒')
AS new
ON DUPLICATE KEY UPDATE label = new.label;

INSERT INTO assistance_type (code, label) VALUES
  ('VERBAL_PROMPT', '语言提示'),
  ('GESTURE_PROMPT', '手势提示'),
  ('DEMONSTRATION', '示范'),
  ('PHYSICAL_ASSISTANCE', '身体辅助'),
  ('REINFORCEMENT', '强化'),
  ('VISUAL_PROMPT', '视觉提示'),
  ('OTHER', '其他')
AS new
ON DUPLICATE KEY UPDATE label = new.label;

INSERT INTO course_behavior_config (course_code, behavior_code, configured_by_user_id) VALUES
  ('CHINESE', 'LEAVE_SEAT', NULL),
  ('CHINESE', 'ATTENTION_DROP', NULL),
  ('CHINESE', 'RAISE_HAND', NULL),
  ('CHINESE', 'ANSWER_QUESTION', NULL),
  ('CHINESE', 'SCREAM', NULL),
  ('CHINESE', 'CRY', NULL),
  ('PHYSICAL_EDUCATION', 'QUEUE', NULL),
  ('PHYSICAL_EDUCATION', 'RUN', NULL),
  ('PHYSICAL_EDUCATION', 'AGGRESSION', NULL),
  ('PHYSICAL_EDUCATION', 'COOPERATION', NULL),
  ('PHYSICAL_EDUCATION', 'FOLLOW_RULES', NULL)
AS new
ON DUPLICATE KEY UPDATE configured_by_user_id = new.configured_by_user_id;
