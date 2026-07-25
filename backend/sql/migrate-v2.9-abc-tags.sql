-- V2.9.0：增加 ABC 详细记录中 A（行为前因）与 C（行为后果）的分组快捷标签字典。

USE special_ed_assistant;

CREATE TABLE IF NOT EXISTS abc_tag_group (
  dimension ENUM('ANTECEDENT', 'CONSEQUENCE') NOT NULL,
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  display_order TINYINT UNSIGNED NOT NULL,
  PRIMARY KEY (dimension, code),
  UNIQUE KEY uk_abc_tag_group_order (dimension, display_order)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS abc_tag_option (
  dimension ENUM('ANTECEDENT', 'CONSEQUENCE') NOT NULL,
  code VARCHAR(64) NOT NULL,
  group_code VARCHAR(64) NOT NULL,
  label VARCHAR(255) NOT NULL,
  display_order TINYINT UNSIGNED NOT NULL,
  requires_custom_text BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (dimension, code),
  UNIQUE KEY uk_abc_tag_option_order (dimension, group_code, display_order),
  CONSTRAINT fk_abc_tag_option_group
    FOREIGN KEY (dimension, group_code)
    REFERENCES abc_tag_group (dimension, code) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_record_abc_tag (
  behavior_record_id BIGINT UNSIGNED NOT NULL,
  dimension ENUM('ANTECEDENT', 'CONSEQUENCE') NOT NULL,
  tag_code VARCHAR(64) NOT NULL,
  custom_text VARCHAR(500) NULL,
  PRIMARY KEY (behavior_record_id, dimension, tag_code),
  KEY idx_behavior_record_abc_tag_option (dimension, tag_code),
  CONSTRAINT chk_behavior_record_abc_tag_custom CHECK (
    (tag_code = 'OTHER' AND NULLIF(TRIM(custom_text), '') IS NOT NULL)
    OR (tag_code <> 'OTHER' AND custom_text IS NULL)
  ),
  CONSTRAINT fk_behavior_record_abc_tag_record
    FOREIGN KEY (behavior_record_id) REFERENCES behavior_record (id) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_record_abc_tag_option
    FOREIGN KEY (dimension, tag_code)
    REFERENCES abc_tag_option (dimension, code)
) ENGINE=InnoDB;

INSERT INTO abc_tag_group (dimension, code, label, display_order) VALUES
  ('ANTECEDENT', 'TASK_INSTRUCTION', '任务与指令类', 1),
  ('ANTECEDENT', 'ENVIRONMENT_SENSORY', '环境与感官类', 2),
  ('ANTECEDENT', 'SOCIAL_INTERACTION', '社交与互动类', 3),
  ('ANTECEDENT', 'ITEM_ACTIVITY', '物品与活动类', 4),
  ('ANTECEDENT', 'PHYSICAL_OTHER', '生理与其它', 5),
  ('CONSEQUENCE', 'TEACHER_RESPONSE', '教师反馈类', 1),
  ('CONSEQUENCE', 'TASK_ENVIRONMENT_ADJUSTMENT', '任务与环境调整类', 2),
  ('CONSEQUENCE', 'PEER_RESPONSE', '同伴反应类', 3),
  ('CONSEQUENCE', 'ASSISTANCE_INTERVENTION', '辅助介入类', 4),
  ('CONSEQUENCE', 'ITEM_ACCESS', '物品获得类', 5),
  ('CONSEQUENCE', 'OTHER', '其他', 6)
AS new
ON DUPLICATE KEY UPDATE
  label = new.label,
  display_order = new.display_order;

INSERT INTO abc_tag_option (
  dimension, code, group_code, label, display_order, requires_custom_text
) VALUES
  ('ANTECEDENT', 'TASK_DIFFICULT', 'TASK_INSTRUCTION', '任务难度过高/无法理解', 1, FALSE),
  ('ANTECEDENT', 'TASK_NEW', 'TASK_INSTRUCTION', '布置新任务/提出新要求', 2, FALSE),
  ('ANTECEDENT', 'TASK_URGED', 'TASK_INSTRUCTION', '被催促完成任务', 3, FALSE),
  ('ANTECEDENT', 'ENV_NOISY', 'ENVIRONMENT_SENSORY', '环境嘈杂/刺激过载', 1, FALSE),
  ('ANTECEDENT', 'TRANSITION', 'ENVIRONMENT_SENSORY', '场景转换/换课件/换教室/换老师', 2, FALSE),
  ('ANTECEDENT', 'UNSTRUCTURED', 'ENVIRONMENT_SENSORY', '非结构化时间（如课间自由活动）', 3, FALSE),
  ('ANTECEDENT', 'PEER_CONFLICT', 'SOCIAL_INTERACTION', '同伴干扰/冲突/嘲笑', 1, FALSE),
  ('ANTECEDENT', 'REQUEST_DENIED', 'SOCIAL_INTERACTION', '需求或提议被拒绝', 2, FALSE),
  ('ANTECEDENT', 'ATTENTION_SHIFT', 'SOCIAL_INTERACTION', '教师或家长关注其他', 3, FALSE),
  ('ANTECEDENT', 'ACTIVITY_STOPPED', 'ITEM_ACTIVITY', '被要求停止喜爱的活动', 1, FALSE),
  ('ANTECEDENT', 'ITEM_DENIED', 'ITEM_ACTIVITY', '拿不到喜爱的物品', 2, FALSE),
  ('ANTECEDENT', 'PHYSICAL_DISCOMFORT', 'PHYSICAL_OTHER', '疑似身体不适/疲劳', 1, FALSE),
  ('ANTECEDENT', 'NO_OBVIOUS_TRIGGER', 'PHYSICAL_OTHER', '无明显外在诱因', 2, FALSE),
  ('ANTECEDENT', 'OTHER', 'PHYSICAL_OTHER', '其他', 3, TRUE),
  ('CONSEQUENCE', 'VERBAL_PROMPT', 'TEACHER_RESPONSE', '口头提醒/讲道理/批评', 1, FALSE),
  ('CONSEQUENCE', 'VERBAL_SOOTHING', 'TEACHER_RESPONSE', '语言安抚/情绪疏导', 2, FALSE),
  ('CONSEQUENCE', 'PLANNED_IGNORING', 'TEACHER_RESPONSE', '战术性忽略/冷处理', 3, FALSE),
  ('CONSEQUENCE', 'TASK_PAUSED', 'TASK_ENVIRONMENT_ADJUSTMENT', '任务暂停/延期', 1, FALSE),
  ('CONSEQUENCE', 'TASK_SIMPLIFIED', 'TASK_ENVIRONMENT_ADJUSTMENT', '降低任务难度/提供替代任务', 2, FALSE),
  ('CONSEQUENCE', 'REMOVED_FROM_ENV', 'TASK_ENVIRONMENT_ADJUSTMENT', '被带离当前环境（如离开教室）', 3, FALSE),
  ('CONSEQUENCE', 'PEER_ATTENTION', 'PEER_RESPONSE', '同伴注视/议论/围观', 1, FALSE),
  ('CONSEQUENCE', 'PEER_YIELDED', 'PEER_RESPONSE', '同伴妥协/退让/给予物品', 2, FALSE),
  ('CONSEQUENCE', 'PHYSICAL_ASSIST', 'ASSISTANCE_INTERVENTION', '给予肢体辅助', 1, FALSE),
  ('CONSEQUENCE', 'VISUAL_ASSIST', 'ASSISTANCE_INTERVENTION', '给予视觉/手势提示', 2, FALSE),
  ('CONSEQUENCE', 'ITEM_GIVEN', 'ITEM_ACCESS', '给予喜爱的物品或活动（作为安抚或强化）', 1, FALSE),
  ('CONSEQUENCE', 'OTHER', 'OTHER', '其他', 1, TRUE)
AS new
ON DUPLICATE KEY UPDATE
  group_code = new.group_code,
  label = new.label,
  display_order = new.display_order,
  requires_custom_text = new.requires_custom_text;

INSERT IGNORE INTO schema_migration (version) VALUES ('2.9.0');
