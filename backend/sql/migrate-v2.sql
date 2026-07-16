-- 从 V0.3 升级到 V2.0.0。
-- 本迁移会重建旧业务表；检测到旧业务数据时会立即中止，不执行破坏性操作。
-- 全新数据库请直接执行 schema.sql，无需执行本文件。

USE special_ed_assistant;

CREATE TABLE IF NOT EXISTS schema_migration (
  version VARCHAR(32) NOT NULL,
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (version)
) ENGINE=InnoDB;

DROP PROCEDURE IF EXISTS migrate_to_v2;

DELIMITER $$

CREATE PROCEDURE migrate_to_v2()
migration: BEGIN
  DECLARE legacy_business_row_count BIGINT UNSIGNED DEFAULT 0;

  IF EXISTS (
    SELECT 1 FROM schema_migration WHERE version = '2.0.0'
  ) THEN
    LEAVE migration;
  END IF;

  SELECT
    (SELECT COUNT(*) FROM observation_session)
    + (SELECT COUNT(*) FROM behavior_record)
    + (SELECT COUNT(*) FROM student_training_plan)
    + (SELECT COUNT(*) FROM teacher_evaluation)
  INTO legacy_business_row_count;

  IF legacy_business_row_count > 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'V2 migration aborted: legacy business tables are not empty';
  END IF;

  DROP TABLE IF EXISTS behavior_record_assistance;
  DROP TABLE IF EXISTS behavior_record;
  DROP TABLE IF EXISTS observation_session;
  DROP TABLE IF EXISTS course_behavior_config;
  DROP TABLE IF EXISTS student_training_plan;
  DROP TABLE IF EXISTS teacher_evaluation;
  DROP TABLE IF EXISTS antecedent_type;
  DROP TABLE IF EXISTS consequence_type;
  DROP TABLE IF EXISTS assistance_type;

  CREATE TABLE IF NOT EXISTS app_user_current_student (
    user_id BIGINT UNSIGNED NOT NULL,
    student_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id),
    KEY idx_app_user_current_student_student (student_id),
    CONSTRAINT fk_app_user_current_student_binding
      FOREIGN KEY (user_id, student_id)
      REFERENCES app_user_student (user_id, student_id)
      ON DELETE CASCADE
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS behavior_stage_type (
    code VARCHAR(64) NOT NULL,
    label VARCHAR(64) NOT NULL,
    PRIMARY KEY (code)
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS behavior_function_type (
    code VARCHAR(64) NOT NULL,
    label VARCHAR(64) NOT NULL,
    PRIMARY KEY (code)
  ) ENGINE=InnoDB;

  CREATE TABLE assistance_type (
    code VARCHAR(64) NOT NULL,
    label VARCHAR(64) NOT NULL,
    group_code ENUM('INTERNAL_STIMULUS', 'EXTERNAL_STIMULUS') NOT NULL,
    display_order SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (code),
    UNIQUE KEY uk_assistance_type_display_order (display_order)
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS class_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id BIGINT UNSIGNED NOT NULL,
    creator_id BIGINT UNSIGNED NOT NULL,
    record_date DATE NOT NULL,
    course_code VARCHAR(64) NOT NULL,
    course_other_description VARCHAR(255) NULL,
    environment_code VARCHAR(64) NOT NULL,
    environment_other_description VARCHAR(255) NULL,
    observation_duration_minutes SMALLINT UNSIGNED NOT NULL,
    overall_remark VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    KEY idx_class_record_student_date (student_id, record_date),
    KEY idx_class_record_creator_student (creator_id, student_id),
    KEY idx_class_record_course (course_code),
    KEY idx_class_record_environment (environment_code),
    CONSTRAINT chk_class_record_duration
      CHECK (observation_duration_minutes > 0),
    CONSTRAINT fk_class_record_creator_student
      FOREIGN KEY (creator_id, student_id)
      REFERENCES app_user_student (user_id, student_id),
    CONSTRAINT fk_class_record_course
      FOREIGN KEY (course_code) REFERENCES course_type (code),
    CONSTRAINT fk_class_record_environment
      FOREIGN KEY (environment_code) REFERENCES environment_type (code)
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS behavior_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    class_record_id BIGINT UNSIGNED NOT NULL,
    creator_id BIGINT UNSIGNED NOT NULL,
    occurred_at DATETIME(3) NOT NULL,
    behavior_code VARCHAR(64) NOT NULL,
    duration_minutes SMALLINT UNSIGNED NULL,
    stage_code VARCHAR(64) NULL,
    antecedent_text VARCHAR(1000) NULL,
    behavior_description VARCHAR(1000) NULL,
    consequence_text VARCHAR(1000) NULL,
    function_code VARCHAR(64) NULL,
    assistance_result_text VARCHAR(1000) NULL,
    detail_saved BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    KEY idx_behavior_record_class_behavior_time
      (class_record_id, behavior_code, occurred_at, id),
    KEY idx_behavior_record_creator (creator_id),
    KEY idx_behavior_record_behavior (behavior_code),
    KEY idx_behavior_record_stage (stage_code),
    KEY idx_behavior_record_function (function_code),
    CONSTRAINT chk_behavior_record_duration
      CHECK (duration_minutes IS NULL OR duration_minutes > 0),
    CONSTRAINT fk_behavior_record_class_record
      FOREIGN KEY (class_record_id) REFERENCES class_record (id) ON DELETE CASCADE,
    CONSTRAINT fk_behavior_record_creator
      FOREIGN KEY (creator_id) REFERENCES app_user (id),
    CONSTRAINT fk_behavior_record_behavior
      FOREIGN KEY (behavior_code) REFERENCES behavior_type (code),
    CONSTRAINT fk_behavior_record_stage
      FOREIGN KEY (stage_code) REFERENCES behavior_stage_type (code),
    CONSTRAINT fk_behavior_record_function
      FOREIGN KEY (function_code) REFERENCES behavior_function_type (code)
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS behavior_record_assistance (
    behavior_record_id BIGINT UNSIGNED NOT NULL,
    assistance_code VARCHAR(64) NOT NULL,
    content VARCHAR(500) NOT NULL,
    PRIMARY KEY (behavior_record_id, assistance_code),
    KEY idx_behavior_record_assistance_type (assistance_code),
    CONSTRAINT chk_behavior_record_assistance_content
      CHECK (CHAR_LENGTH(TRIM(content)) > 0),
    CONSTRAINT fk_behavior_record_assistance_record
      FOREIGN KEY (behavior_record_id) REFERENCES behavior_record (id) ON DELETE CASCADE,
    CONSTRAINT fk_behavior_record_assistance_type
      FOREIGN KEY (assistance_code) REFERENCES assistance_type (code)
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS course_behavior_config (
    course_code VARCHAR(64) NOT NULL,
    behavior_code VARCHAR(64) NOT NULL,
    configured_by_user_id BIGINT UNSIGNED NULL,
    PRIMARY KEY (course_code, behavior_code),
    KEY idx_course_behavior_config_behavior (behavior_code),
    KEY idx_course_behavior_config_user (configured_by_user_id),
    CONSTRAINT fk_course_behavior_config_course
      FOREIGN KEY (course_code) REFERENCES course_type (code),
    CONSTRAINT fk_course_behavior_config_behavior
      FOREIGN KEY (behavior_code) REFERENCES behavior_type (code),
    CONSTRAINT fk_course_behavior_config_user
      FOREIGN KEY (configured_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS training_goal_category (
    code VARCHAR(64) NOT NULL,
    label VARCHAR(128) NOT NULL,
    display_order SMALLINT UNSIGNED NOT NULL,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (code),
    UNIQUE KEY uk_training_goal_category_display_order (display_order)
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS training_goal (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_code VARCHAR(64) NOT NULL,
    goal_text VARCHAR(500) NOT NULL,
    goal_type ENUM('STANDARD', 'CUSTOM') NOT NULL,
    owner_student_id BIGINT UNSIGNED NULL,
    PRIMARY KEY (id),
    KEY idx_training_goal_category_type (category_code, goal_type),
    KEY idx_training_goal_owner (owner_student_id),
    CONSTRAINT chk_training_goal_text
      CHECK (CHAR_LENGTH(TRIM(goal_text)) > 0),
    CONSTRAINT chk_training_goal_owner
      CHECK (
        (goal_type = 'STANDARD' AND owner_student_id IS NULL)
        OR (goal_type = 'CUSTOM' AND owner_student_id IS NOT NULL)
      ),
    CONSTRAINT fk_training_goal_category
      FOREIGN KEY (category_code) REFERENCES training_goal_category (code),
    CONSTRAINT fk_training_goal_owner
      FOREIGN KEY (owner_student_id) REFERENCES student (id) ON DELETE CASCADE
  ) ENGINE=InnoDB;

  CREATE TABLE IF NOT EXISTS student_training_goal (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id BIGINT UNSIGNED NOT NULL,
    goal_id BIGINT UNSIGNED NOT NULL,
    initial_level VARCHAR(32) NOT NULL,
    current_level VARCHAR(32) NOT NULL,
    phase TINYINT UNSIGNED NOT NULL DEFAULT 1,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'PAUSED')
      NOT NULL DEFAULT 'NOT_STARTED',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_training_goal_student_goal (student_id, goal_id),
    KEY idx_student_training_goal_student_status (student_id, status),
    KEY idx_student_training_goal_goal (goal_id),
    CONSTRAINT chk_student_training_goal_initial_level
      CHECK (CHAR_LENGTH(TRIM(initial_level)) > 0),
    CONSTRAINT chk_student_training_goal_current_level
      CHECK (CHAR_LENGTH(TRIM(current_level)) > 0),
    CONSTRAINT chk_student_training_goal_phase CHECK (phase BETWEEN 1 AND 3),
    CONSTRAINT fk_student_training_goal_student
      FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE CASCADE,
    CONSTRAINT fk_student_training_goal_goal
      FOREIGN KEY (goal_id) REFERENCES training_goal (id) ON DELETE CASCADE
  ) ENGINE=InnoDB;

  INSERT INTO schema_migration (version) VALUES ('2.0.0');
END$$

DELIMITER ;

CALL migrate_to_v2();
DROP PROCEDURE migrate_to_v2;
