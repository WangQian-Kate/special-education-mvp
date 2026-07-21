-- 从 V2.3.0 增加确认后的 4 项行为功能字典。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_behavior_functions;

DELIMITER $$

CREATE PROCEDURE migrate_behavior_functions()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM behavior_record
    WHERE function_code IS NOT NULL
      AND function_code NOT IN ('ATTENTION', 'TANGIBLE', 'ESCAPE', 'SENSORY')
  ) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '检测到非标准行为功能业务数据，请先完成编码映射后再执行 V2.4 迁移';
  END IF;

  DELETE FROM behavior_function_type
  WHERE code NOT IN ('ATTENTION', 'TANGIBLE', 'ESCAPE', 'SENSORY');

  INSERT INTO behavior_function_type (code, label) VALUES
    ('ATTENTION', '获取关注'),
    ('TANGIBLE', '获取实物'),
    ('ESCAPE', '逃避'),
    ('SENSORY', '感官刺激')
  AS new
  ON DUPLICATE KEY UPDATE label = new.label;

  INSERT IGNORE INTO schema_migration (version) VALUES ('2.4.0');
END$$

DELIMITER ;

CALL migrate_behavior_functions();
DROP PROCEDURE migrate_behavior_functions;
