-- 从 V2.2.0 升级辅助方式字典，并允许勾选辅助方式后不填写补充内容。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_assistance_validation;

DELIMITER $$

CREATE PROCEDURE migrate_assistance_validation()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM behavior_record_assistance
    WHERE assistance_code NOT IN (
      'ADD_EXTERNAL_OBJECT',
      'CHANGE_TARGET_SIZE',
      'FULL_BODY_ASSISTANCE',
      'HALF_BODY_ASSISTANCE',
      'POSTURE_ASSISTANCE',
      'POSITION_ASSISTANCE',
      'VERBAL_ASSISTANCE',
      'DEMONSTRATION_ASSISTANCE',
      'VISUAL_ASSISTANCE'
    )
  ) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '检测到旧辅助方式业务数据，请先完成编码映射后再执行 V2.3 迁移';
  END IF;

  DELETE FROM assistance_type
  WHERE code NOT IN (
    'ADD_EXTERNAL_OBJECT',
    'CHANGE_TARGET_SIZE',
    'FULL_BODY_ASSISTANCE',
    'HALF_BODY_ASSISTANCE',
    'POSTURE_ASSISTANCE',
    'POSITION_ASSISTANCE',
    'VERBAL_ASSISTANCE',
    'DEMONSTRATION_ASSISTANCE',
    'VISUAL_ASSISTANCE'
  );

  UPDATE assistance_type
  SET display_order = display_order + 100
  WHERE code IN (
    'ADD_EXTERNAL_OBJECT',
    'CHANGE_TARGET_SIZE',
    'FULL_BODY_ASSISTANCE',
    'HALF_BODY_ASSISTANCE',
    'POSTURE_ASSISTANCE',
    'POSITION_ASSISTANCE',
    'VERBAL_ASSISTANCE',
    'DEMONSTRATION_ASSISTANCE',
    'VISUAL_ASSISTANCE'
  );

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

  IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'behavior_record_assistance'
      AND constraint_name = 'chk_behavior_record_assistance_content'
  ) THEN
    ALTER TABLE behavior_record_assistance
      DROP CHECK chk_behavior_record_assistance_content;
  END IF;

  ALTER TABLE behavior_record_assistance
    MODIFY COLUMN content VARCHAR(500) NULL;

  INSERT IGNORE INTO schema_migration (version) VALUES ('2.3.0');
END$$

DELIMITER ;

CALL migrate_assistance_validation();
DROP PROCEDURE migrate_assistance_validation;
