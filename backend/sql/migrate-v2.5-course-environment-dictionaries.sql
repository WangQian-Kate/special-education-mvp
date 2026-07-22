-- 从 V2.4.0 将课程和环境字典收敛为前端确认的完整选项。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_course_environment_dictionaries;

DELIMITER $$

CREATE PROCEDURE migrate_course_environment_dictionaries()
BEGIN
  INSERT INTO course_type (code, label) VALUES
    ('CHINESE', '语文'),
    ('MATHEMATICS', '数学'),
    ('ENGLISH', '英语'),
    ('PHYSICAL_EDUCATION', '体育'),
    ('MUSIC', '音乐'),
    ('ART', '美术'),
    ('SCIENCE', '科学'),
    ('MORAL_EDUCATION', '道法'),
    ('PHYSICAL_TRAINING', '体能'),
    ('INDIVIDUAL_TRAINING', '个训'),
    ('BREAK', '课间'),
    ('LUNCH', '午餐'),
    ('NOON_REST', '午休'),
    ('SELF_STUDY', '自习'),
    ('OTHER', '其他'),
    ('ALL_DAY_SUMMARY', '全天汇总')
  AS new
  ON DUPLICATE KEY UPDATE label = new.label;

  UPDATE class_record cr
  JOIN course_type ct ON ct.code = cr.course_code
  SET cr.course_other_description = COALESCE(NULLIF(TRIM(cr.course_other_description), ''), ct.label),
      cr.course_code = 'OTHER'
  WHERE cr.course_code NOT IN (
    'CHINESE', 'MATHEMATICS', 'ENGLISH', 'PHYSICAL_EDUCATION',
    'MUSIC', 'ART', 'SCIENCE', 'MORAL_EDUCATION', 'PHYSICAL_TRAINING',
    'INDIVIDUAL_TRAINING', 'BREAK', 'LUNCH', 'NOON_REST', 'SELF_STUDY',
    'OTHER', 'ALL_DAY_SUMMARY'
  );

  INSERT IGNORE INTO course_behavior_config (
    course_code, behavior_code, configured_by_user_id
  )
  SELECT 'OTHER', behavior_code, configured_by_user_id
  FROM course_behavior_config
  WHERE course_code NOT IN (
    'CHINESE', 'MATHEMATICS', 'ENGLISH', 'PHYSICAL_EDUCATION',
    'MUSIC', 'ART', 'SCIENCE', 'MORAL_EDUCATION', 'PHYSICAL_TRAINING',
    'INDIVIDUAL_TRAINING', 'BREAK', 'LUNCH', 'NOON_REST', 'SELF_STUDY',
    'OTHER', 'ALL_DAY_SUMMARY'
  );

  DELETE FROM course_behavior_config
  WHERE course_code NOT IN (
    'CHINESE', 'MATHEMATICS', 'ENGLISH', 'PHYSICAL_EDUCATION',
    'MUSIC', 'ART', 'SCIENCE', 'MORAL_EDUCATION', 'PHYSICAL_TRAINING',
    'INDIVIDUAL_TRAINING', 'BREAK', 'LUNCH', 'NOON_REST', 'SELF_STUDY',
    'OTHER', 'ALL_DAY_SUMMARY'
  );

  DELETE FROM course_type
  WHERE code NOT IN (
    'CHINESE', 'MATHEMATICS', 'ENGLISH', 'PHYSICAL_EDUCATION',
    'MUSIC', 'ART', 'SCIENCE', 'MORAL_EDUCATION', 'PHYSICAL_TRAINING',
    'INDIVIDUAL_TRAINING', 'BREAK', 'LUNCH', 'NOON_REST', 'SELF_STUDY',
    'OTHER', 'ALL_DAY_SUMMARY'
  );

  INSERT INTO environment_type (code, label) VALUES
    ('CLASSROOM', '普通教室'),
    ('RESOURCE_CLASSROOM', '资源教室'),
    ('PLAYGROUND', '操场'),
    ('CORRIDOR', '楼道'),
    ('RESTROOM', '卫生间'),
    ('OFF_CAMPUS', '校外'),
    ('OTHER', '其他')
  AS new
  ON DUPLICATE KEY UPDATE label = new.label;

  UPDATE class_record cr
  JOIN environment_type et ON et.code = cr.environment_code
  SET cr.environment_other_description = COALESCE(NULLIF(TRIM(cr.environment_other_description), ''), et.label),
      cr.environment_code = 'OTHER'
  WHERE cr.environment_code NOT IN (
    'CLASSROOM', 'RESOURCE_CLASSROOM', 'PLAYGROUND', 'CORRIDOR',
    'RESTROOM', 'OFF_CAMPUS', 'OTHER'
  );

  DELETE FROM environment_type
  WHERE code NOT IN (
    'CLASSROOM', 'RESOURCE_CLASSROOM', 'PLAYGROUND', 'CORRIDOR',
    'RESTROOM', 'OFF_CAMPUS', 'OTHER'
  );

  INSERT IGNORE INTO schema_migration (version) VALUES ('2.5.0');
END$$

DELIMITER ;

CALL migrate_course_environment_dictionaries();
DROP PROCEDURE migrate_course_environment_dictionaries;
