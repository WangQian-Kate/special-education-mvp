CREATE DATABASE IF NOT EXISTS special_ed_assistant
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE special_ed_assistant;

CREATE TABLE IF NOT EXISTS schema_migration (
  version VARCHAR(32) NOT NULL,
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (version)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  teacher_id VARCHAR(16) NULL,
  avatar VARCHAR(500) NULL,
  name VARCHAR(64) NOT NULL,
  school VARCHAR(128) NULL,
  position VARCHAR(64) NULL,
  role ENUM('RESOURCE_TEACHER', 'SHADOW_TEACHER', 'PARENT') NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_user_teacher_id (teacher_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS wechat_identity (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  app_id VARCHAR(32) NOT NULL,
  openid VARCHAR(64) NOT NULL,
  unionid VARCHAR(64) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  last_login_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_wechat_identity_app_openid (app_id, openid),
  UNIQUE KEY uk_wechat_identity_app_user (app_id, user_id),
  KEY idx_wechat_identity_user (user_id),
  KEY idx_wechat_identity_unionid (unionid),
  CONSTRAINT fk_wechat_identity_user
    FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS teacher_binding_code (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  code_hash CHAR(64) NOT NULL,
  expires_at DATETIME(3) NOT NULL,
  consumed_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_teacher_binding_code_hash (code_hash),
  KEY idx_teacher_binding_code_user_state (user_id, consumed_at, expires_at),
  CONSTRAINT fk_teacher_binding_code_user
    FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS auth_session (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL,
  expires_at DATETIME(3) NOT NULL,
  revoked_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_session_token_hash (token_hash),
  KEY idx_auth_session_user_state (user_id, revoked_at, expires_at),
  KEY idx_auth_session_expiry (expires_at),
  CONSTRAINT fk_auth_session_user
    FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  student_code VARCHAR(16) NOT NULL,
  name VARCHAR(64) NOT NULL,
  gender ENUM('MALE', 'FEMALE') NULL,
  age TINYINT UNSIGNED NULL,
  class_name VARCHAR(64) NULL,
  disability_type VARCHAR(64) NULL,
  support_goal VARCHAR(255) NULL,
  remark VARCHAR(500) NULL,
  social_adaptation VARCHAR(16) NULL COMMENT '社会适应能力',
  self_management VARCHAR(16) NULL COMMENT '自我管理能力',
  cognitive_level VARCHAR(16) NULL COMMENT '认知水平',
  language_comprehension VARCHAR(16) NULL COMMENT '语言理解',
  expression_ability VARCHAR(16) NULL COMMENT '表达能力',
  hobbies VARCHAR(500) NULL COMMENT '爱好',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_student_code (student_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_user_student (
  user_id BIGINT UNSIGNED NOT NULL,
  student_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (user_id, student_id),
  KEY idx_app_user_student_student (student_id),
  CONSTRAINT fk_app_user_student_user
    FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_app_user_student_student
    FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE CASCADE
) ENGINE=InnoDB;

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

CREATE TABLE IF NOT EXISTS course_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS environment_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_module (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(128) NOT NULL,
  display_order SMALLINT UNSIGNED NOT NULL,
  target_start SMALLINT UNSIGNED NOT NULL,
  target_end SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (code),
  UNIQUE KEY uk_behavior_module_display_order (display_order)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_catalog_metadata (
  behavior_code VARCHAR(64) NOT NULL,
  module_code VARCHAR(64) NOT NULL,
  display_order SMALLINT UNSIGNED NOT NULL,
  has_sub_behaviors BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (behavior_code),
  UNIQUE KEY uk_behavior_catalog_module_order (module_code, display_order),
  CONSTRAINT fk_behavior_catalog_metadata_behavior
    FOREIGN KEY (behavior_code) REFERENCES behavior_type (code) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_catalog_metadata_module
    FOREIGN KEY (module_code) REFERENCES behavior_module (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_group (
  code VARCHAR(64) NOT NULL,
  module_code VARCHAR(64) NOT NULL,
  label VARCHAR(128) NOT NULL,
  display_order SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (code),
  UNIQUE KEY uk_behavior_group_module_order (module_code, display_order),
  CONSTRAINT fk_behavior_group_module
    FOREIGN KEY (module_code) REFERENCES behavior_module (code) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_group_status_option (
  code VARCHAR(64) NOT NULL,
  group_code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  display_order SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (code),
  UNIQUE KEY uk_behavior_group_status_order (group_code, display_order),
  CONSTRAINT fk_behavior_group_status_group
    FOREIGN KEY (group_code) REFERENCES behavior_group (code) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_group_item (
  group_code VARCHAR(64) NOT NULL,
  behavior_code VARCHAR(64) NOT NULL,
  display_order SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (group_code, behavior_code),
  KEY idx_behavior_group_item_behavior (behavior_code),
  CONSTRAINT fk_behavior_group_item_group
    FOREIGN KEY (group_code) REFERENCES behavior_group (code) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_group_item_behavior
    FOREIGN KEY (behavior_code) REFERENCES behavior_type (code) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_catalog_option (
  code VARCHAR(64) NOT NULL,
  behavior_code VARCHAR(64) NOT NULL,
  option_type ENUM('SUB_BEHAVIOR', 'PERFORMANCE') NOT NULL,
  parent_option_code VARCHAR(64) NULL,
  label VARCHAR(255) NOT NULL,
  display_order SMALLINT UNSIGNED NOT NULL,
  requires_custom_text BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (code),
  KEY idx_behavior_catalog_option_behavior_type
    (behavior_code, option_type, display_order),
  KEY idx_behavior_catalog_option_parent (parent_option_code),
  CONSTRAINT fk_behavior_catalog_option_behavior
    FOREIGN KEY (behavior_code) REFERENCES behavior_type (code) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_catalog_option_parent
    FOREIGN KEY (parent_option_code) REFERENCES behavior_catalog_option (code) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_environment_config (
  environment_code VARCHAR(64) NOT NULL,
  behavior_code VARCHAR(64) NOT NULL,
  PRIMARY KEY (environment_code, behavior_code),
  KEY idx_behavior_environment_config_behavior (behavior_code),
  CONSTRAINT fk_behavior_environment_config_environment
    FOREIGN KEY (environment_code) REFERENCES environment_type (code),
  CONSTRAINT fk_behavior_environment_config_behavior
    FOREIGN KEY (behavior_code) REFERENCES behavior_type (code) ON DELETE CASCADE
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

CREATE TABLE IF NOT EXISTS behavior_status_type (
  code VARCHAR(32) NOT NULL,
  label VARCHAR(64) NOT NULL,
  display_order TINYINT UNSIGNED NOT NULL,
  PRIMARY KEY (code),
  UNIQUE KEY uk_behavior_status_type_display_order (display_order)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS assistance_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  group_code ENUM('INTERNAL_STIMULUS', 'EXTERNAL_STIMULUS') NOT NULL,
  display_order SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (code),
  UNIQUE KEY uk_assistance_type_display_order (display_order)
) ENGINE=InnoDB;

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
  KEY idx_class_record_student_date_dimensions
    (student_id, record_date, course_code, environment_code),
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
  sub_behavior_code VARCHAR(64) NULL,
  duration_minutes SMALLINT UNSIGNED NULL,
  stage_code VARCHAR(64) NULL,
  antecedent_text VARCHAR(1000) NULL,
  behavior_description VARCHAR(1000) NULL,
  consequence_text VARCHAR(1000) NULL,
  function_code VARCHAR(64) NULL,
  function_other_text VARCHAR(500) NULL,
  status_code VARCHAR(32) NULL,
  assistance_result_text VARCHAR(1000) NULL,
  detail_saved BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  KEY idx_behavior_record_class_behavior_time
    (class_record_id, behavior_code, occurred_at, id),
  KEY idx_behavior_record_creator (creator_id),
  KEY idx_behavior_record_behavior (behavior_code),
  KEY idx_behavior_record_sub_behavior (sub_behavior_code),
  KEY idx_behavior_record_stage (stage_code),
  KEY idx_behavior_record_function (function_code),
  KEY idx_behavior_record_status (status_code),
  KEY idx_behavior_record_reporting
    (class_record_id, detail_saved, function_code, status_code),
  CONSTRAINT chk_behavior_record_duration
    CHECK (duration_minutes IS NULL OR duration_minutes > 0),
  CONSTRAINT fk_behavior_record_class_record
    FOREIGN KEY (class_record_id) REFERENCES class_record (id) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_record_creator
    FOREIGN KEY (creator_id) REFERENCES app_user (id),
  CONSTRAINT fk_behavior_record_behavior
    FOREIGN KEY (behavior_code) REFERENCES behavior_type (code),
  CONSTRAINT fk_behavior_record_sub_behavior
    FOREIGN KEY (sub_behavior_code) REFERENCES behavior_catalog_option (code),
  CONSTRAINT fk_behavior_record_stage
    FOREIGN KEY (stage_code) REFERENCES behavior_stage_type (code),
  CONSTRAINT fk_behavior_record_function
    FOREIGN KEY (function_code) REFERENCES behavior_function_type (code),
  CONSTRAINT fk_behavior_record_status
    FOREIGN KEY (status_code) REFERENCES behavior_status_type (code),
  CONSTRAINT chk_behavior_record_function_other
    CHECK (
      (function_code = 'OTHER' AND NULLIF(TRIM(function_other_text), '') IS NOT NULL)
      OR (function_code <> 'OTHER' AND function_other_text IS NULL)
      OR (function_code IS NULL AND function_other_text IS NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_record_assistance (
  behavior_record_id BIGINT UNSIGNED NOT NULL,
  assistance_code VARCHAR(64) NOT NULL,
  content VARCHAR(500) NULL,
  PRIMARY KEY (behavior_record_id, assistance_code),
  KEY idx_behavior_record_assistance_type (assistance_code),
  CONSTRAINT fk_behavior_record_assistance_record
    FOREIGN KEY (behavior_record_id) REFERENCES behavior_record (id) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_record_assistance_type
    FOREIGN KEY (assistance_code) REFERENCES assistance_type (code)
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

CREATE TABLE IF NOT EXISTS behavior_record_catalog_selection (
  behavior_record_id BIGINT UNSIGNED NOT NULL,
  option_code VARCHAR(64) NOT NULL,
  custom_text VARCHAR(500) NULL,
  PRIMARY KEY (behavior_record_id, option_code),
  KEY idx_behavior_record_catalog_option (option_code),
  CONSTRAINT fk_behavior_record_catalog_selection_record
    FOREIGN KEY (behavior_record_id) REFERENCES behavior_record (id) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_record_catalog_selection_option
    FOREIGN KEY (option_code) REFERENCES behavior_catalog_option (code)
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
  standard_number SMALLINT UNSIGNED NULL,
  category_code VARCHAR(64) NOT NULL,
  goal_text VARCHAR(500) NOT NULL,
  goal_type ENUM('STANDARD', 'CUSTOM') NOT NULL,
  owner_student_id BIGINT UNSIGNED NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_training_goal_standard_number (standard_number),
  KEY idx_training_goal_category_type (category_code, goal_type),
  KEY idx_training_goal_owner (owner_student_id),
  CONSTRAINT chk_training_goal_text
    CHECK (CHAR_LENGTH(TRIM(goal_text)) > 0),
  CONSTRAINT chk_training_goal_standard_number
    CHECK (
      (goal_type = 'STANDARD' AND standard_number IS NOT NULL)
      OR (goal_type = 'CUSTOM' AND standard_number IS NULL)
    ),
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

CREATE TABLE IF NOT EXISTS behavior_training_goal (
  behavior_code VARCHAR(64) NOT NULL,
  standard_number SMALLINT UNSIGNED NOT NULL,
  sub_behavior_code VARCHAR(64) NULL,
  PRIMARY KEY (behavior_code, standard_number),
  KEY idx_behavior_training_goal_number (standard_number),
  KEY idx_behavior_training_goal_sub_behavior (sub_behavior_code),
  CONSTRAINT fk_behavior_training_goal_behavior
    FOREIGN KEY (behavior_code) REFERENCES behavior_type (code) ON DELETE CASCADE,
  CONSTRAINT fk_behavior_training_goal_goal
    FOREIGN KEY (standard_number) REFERENCES training_goal (standard_number),
  CONSTRAINT fk_behavior_training_goal_sub_behavior
    FOREIGN KEY (sub_behavior_code) REFERENCES behavior_catalog_option (code) ON DELETE CASCADE
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

INSERT IGNORE INTO schema_migration (version) VALUES ('2.0.0');
INSERT IGNORE INTO schema_migration (version) VALUES ('3.0.0');
INSERT IGNORE INTO schema_migration (version) VALUES ('3.1.0');
