-- V2.8.0：补齐学生评估页每日评价表及行为记录子行为字段。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_v28_daily_evaluation;

DELIMITER $$

CREATE PROCEDURE migrate_v28_daily_evaluation()
BEGIN
  CREATE TABLE IF NOT EXISTS daily_evaluation (
    student_id BIGINT UNSIGNED NOT NULL,
    record_date DATE NOT NULL,
    emotion VARCHAR(500) NULL,
    adaptation VARCHAR(500) NULL,
    social VARCHAR(500) NULL,
    self_mgmt VARCHAR(500) NULL,
    language VARCHAR(500) NULL,
    focus VARCHAR(500) NULL,
    PRIMARY KEY (student_id, record_date),
    CONSTRAINT fk_daily_evaluation_student
      FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE CASCADE
  ) ENGINE=InnoDB;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND column_name = 'sub_behavior_code'
  ) THEN
    ALTER TABLE behavior_record
      ADD COLUMN sub_behavior_code VARCHAR(64) NULL AFTER behavior_code;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND index_name = 'idx_behavior_record_sub_behavior'
  ) THEN
    ALTER TABLE behavior_record
      ADD KEY idx_behavior_record_sub_behavior (sub_behavior_code);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'behavior_record'
      AND constraint_name = 'fk_behavior_record_sub_behavior'
  ) THEN
    ALTER TABLE behavior_record
      ADD CONSTRAINT fk_behavior_record_sub_behavior
      FOREIGN KEY (sub_behavior_code) REFERENCES behavior_catalog_option (code);
  END IF;

  INSERT IGNORE INTO schema_migration (version) VALUES ('2.8.0');
END$$

DELIMITER ;

CALL migrate_v28_daily_evaluation();
DROP PROCEDURE migrate_v28_daily_evaluation;
