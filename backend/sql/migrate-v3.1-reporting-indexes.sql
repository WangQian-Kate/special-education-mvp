-- V3.1.0：补充学生评估统计的覆盖索引。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_v31_reporting_indexes;

DELIMITER $$

CREATE PROCEDURE migrate_v31_reporting_indexes()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'class_record'
      AND index_name = 'idx_class_record_student_date_dimensions'
  ) THEN
    ALTER TABLE class_record
      ADD KEY idx_class_record_student_date_dimensions
        (student_id, record_date, course_code, environment_code);
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'behavior_record'
      AND index_name = 'idx_behavior_record_reporting'
  ) THEN
    ALTER TABLE behavior_record
      ADD KEY idx_behavior_record_reporting
        (class_record_id, detail_saved, function_code, status_code);
  END IF;
END$$

DELIMITER ;

CALL migrate_v31_reporting_indexes();
DROP PROCEDURE migrate_v31_reporting_indexes;

INSERT IGNORE INTO schema_migration (version) VALUES ('3.1.0');
