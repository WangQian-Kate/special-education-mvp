-- V2.7.0：行为完成状态、行为功能“其他”及统计接口所需索引。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_v27_reporting_status;

DELIMITER $$

CREATE PROCEDURE migrate_v27_reporting_status()
BEGIN
  CREATE TABLE IF NOT EXISTS behavior_status_type (
    code VARCHAR(32) NOT NULL,
    label VARCHAR(64) NOT NULL,
    display_order TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (code),
    UNIQUE KEY uk_behavior_status_type_display_order (display_order)
  ) ENGINE=InnoDB;

  INSERT INTO behavior_status_type (code, label, display_order) VALUES
    ('INCOMPLETE', '未完成', 1),
    ('ASSISTED', '辅助完成', 2),
    ('INDEPENDENT', '独立完成', 3)
  AS new
  ON DUPLICATE KEY UPDATE label = new.label, display_order = new.display_order;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND column_name = 'function_other_text'
  ) THEN
    ALTER TABLE behavior_record
      ADD COLUMN function_other_text VARCHAR(500) NULL AFTER function_code;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND column_name = 'status_code'
  ) THEN
    ALTER TABLE behavior_record
      ADD COLUMN status_code VARCHAR(32) NULL AFTER function_other_text;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND index_name = 'idx_behavior_record_status'
  ) THEN
    ALTER TABLE behavior_record ADD KEY idx_behavior_record_status (status_code);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND constraint_name = 'fk_behavior_record_status'
  ) THEN
    ALTER TABLE behavior_record
      ADD CONSTRAINT fk_behavior_record_status
      FOREIGN KEY (status_code) REFERENCES behavior_status_type (code);
  END IF;

  IF EXISTS (
    SELECT 1 FROM behavior_record
    WHERE function_code IS NOT NULL
      AND function_code NOT IN ('ATTENTION', 'TANGIBLE', 'ESCAPE', 'SENSORY', 'OTHER')
  ) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '检测到非标准行为功能业务数据，请先完成编码映射后再执行 V2.7 迁移';
  END IF;

  INSERT INTO behavior_function_type (code, label) VALUES
    ('ATTENTION', '获取关注'),
    ('TANGIBLE', '获取实物'),
    ('ESCAPE', '逃避/回避'),
    ('SENSORY', '感觉刺激'),
    ('OTHER', '其他')
  AS new
  ON DUPLICATE KEY UPDATE label = new.label;

  DELETE FROM behavior_function_type
  WHERE code NOT IN ('ATTENTION', 'TANGIBLE', 'ESCAPE', 'SENSORY', 'OTHER');

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND constraint_name = 'chk_behavior_record_function_other'
  ) THEN
    ALTER TABLE behavior_record
      ADD CONSTRAINT chk_behavior_record_function_other CHECK (
        (function_code = 'OTHER' AND NULLIF(TRIM(function_other_text), '') IS NOT NULL)
        OR (function_code <> 'OTHER' AND function_other_text IS NULL)
        OR (function_code IS NULL AND function_other_text IS NULL)
      );
  END IF;

  INSERT IGNORE INTO schema_migration (version) VALUES ('2.7.0');
END$$

DELIMITER ;

CALL migrate_v27_reporting_status();
DROP PROCEDURE migrate_v27_reporting_status;
