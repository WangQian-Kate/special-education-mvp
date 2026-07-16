USE special_ed_assistant;

INSERT INTO app_user (id, name, role) VALUES
  (1, '演示教师', 'RESOURCE_TEACHER')
ON DUPLICATE KEY UPDATE name = VALUES(name), role = VALUES(role);

INSERT INTO student (id, name, age, class_name, support_goal) VALUES
  (1, '演示学生', 9, '三年级一班', '提升课堂任务持续参与能力')
ON DUPLICATE KEY UPDATE
  name = VALUES(name), age = VALUES(age), class_name = VALUES(class_name), support_goal = VALUES(support_goal);

INSERT INTO antecedent_type (code, label) VALUES
  ('TEACHER_QUESTION', '教师提问'),
  ('TASK_SWITCH', '任务切换'),
  ('GROUP_ACTIVITY', '小组活动')
ON DUPLICATE KEY UPDATE label = VALUES(label);

INSERT INTO behavior_type (code, label) VALUES
  ('ATTENTION_DROP', '注意力下降'),
  ('LEAVE_SEAT', '离座'),
  ('TASK_REFUSAL', '拒绝任务'),
  ('EMOTIONAL_FLUCTUATION', '情绪波动'),
  ('NO_RESPONSE', '无指令响应'),
  ('PEER_CONFLICT', '同伴冲突')
ON DUPLICATE KEY UPDATE label = VALUES(label);

INSERT INTO consequence_type (code, label) VALUES
  ('VERBAL_PROMPT', '教师提醒'),
  ('GESTURE_PROMPT', '手势提示'),
  ('TASK_BREAKDOWN', '拆分任务'),
  ('SHORT_BREAK', '短暂休息')
ON DUPLICATE KEY UPDATE label = VALUES(label);
