-- 从 V2.0.0 升级标准训练目标编号结构。
-- 执行本文件后必须继续执行 seed.sql，写入 165 项标准目标并记录 2.1.0 版本。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_training_goal_number;

DELIMITER $$

CREATE PROCEDURE migrate_training_goal_number()
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'training_goal'
      AND column_name = 'standard_number'
  ) THEN
    ALTER TABLE training_goal
      ADD COLUMN standard_number SMALLINT UNSIGNED NULL AFTER id;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'training_goal'
      AND index_name = 'uk_training_goal_standard_number'
  ) THEN
    ALTER TABLE training_goal
      ADD UNIQUE KEY uk_training_goal_standard_number (standard_number);
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'training_goal'
      AND constraint_name = 'chk_training_goal_standard_number'
  ) THEN
    ALTER TABLE training_goal DROP CHECK chk_training_goal_standard_number;
  END IF;

  ALTER TABLE training_goal
    ADD CONSTRAINT chk_training_goal_standard_number
    CHECK (
      (goal_type = 'STANDARD' AND standard_number IS NOT NULL)
      OR (goal_type = 'CUSTOM' AND standard_number IS NULL)
    );
END$$

DELIMITER ;

CALL migrate_training_goal_number();
DROP PROCEDURE migrate_training_goal_number;
