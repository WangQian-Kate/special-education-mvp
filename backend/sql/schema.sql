CREATE DATABASE IF NOT EXISTS special_ed_assistant
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE special_ed_assistant;

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  avatar VARCHAR(500) NULL,
  name VARCHAR(64) NOT NULL,
  school VARCHAR(128) NULL,
  position VARCHAR(64) NULL,
  role ENUM('RESOURCE_TEACHER', 'SHADOW_TEACHER', 'PARENT') NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  age TINYINT UNSIGNED NULL,
  class_name VARCHAR(64) NULL,
  disability_type VARCHAR(64) NULL,
  support_goal VARCHAR(255) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id)
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

CREATE TABLE IF NOT EXISTS antecedent_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS consequence_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS assistance_type (
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS observation_session (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  student_id BIGINT UNSIGNED NOT NULL,
  creator_id BIGINT UNSIGNED NOT NULL,
  observation_date DATE NOT NULL,
  course_code VARCHAR(64) NOT NULL,
  course_other_description VARCHAR(255) NULL,
  environment_code VARCHAR(64) NOT NULL,
  environment_other_description VARCHAR(255) NULL,
  observation_duration_minutes SMALLINT UNSIGNED NOT NULL,
  period_behavior_remark VARCHAR(1000) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_observation_session_id_student (id, student_id),
  KEY idx_observation_session_student_date (student_id, observation_date),
  KEY idx_observation_session_creator (creator_id),
  KEY idx_observation_session_course (course_code),
  KEY idx_observation_session_environment (environment_code),
  CONSTRAINT chk_observation_session_duration
    CHECK (observation_duration_minutes > 0),
  CONSTRAINT fk_observation_session_student
    FOREIGN KEY (student_id) REFERENCES student (id),
  CONSTRAINT fk_observation_session_creator
    FOREIGN KEY (creator_id) REFERENCES app_user (id),
  CONSTRAINT fk_observation_session_course
    FOREIGN KEY (course_code) REFERENCES course_type (code),
  CONSTRAINT fk_observation_session_environment
    FOREIGN KEY (environment_code) REFERENCES environment_type (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  observation_session_id BIGINT UNSIGNED NOT NULL,
  student_id BIGINT UNSIGNED NOT NULL,
  creator_id BIGINT UNSIGNED NOT NULL,
  record_time DATETIME(3) NOT NULL,
  antecedent_code VARCHAR(64) NULL,
  behavior_code VARCHAR(64) NOT NULL,
  consequence_code VARCHAR(64) NULL,
  assistance_result ENUM('SUCCESS', 'PARTIAL_SUCCESS', 'FAILED') NULL,
  assistance_other_description VARCHAR(255) NULL,
  frequency INT UNSIGNED NOT NULL DEFAULT 1,
  duration_seconds INT UNSIGNED NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_behavior_record_session_time (observation_session_id, record_time),
  KEY idx_behavior_record_student_time (student_id, record_time),
  KEY idx_behavior_record_creator (creator_id),
  KEY idx_behavior_record_antecedent (antecedent_code),
  KEY idx_behavior_record_behavior (behavior_code),
  KEY idx_behavior_record_consequence (consequence_code),
  CONSTRAINT chk_behavior_record_frequency CHECK (frequency > 0),
  CONSTRAINT fk_behavior_record_session_student
    FOREIGN KEY (observation_session_id, student_id)
    REFERENCES observation_session (id, student_id),
  CONSTRAINT fk_behavior_record_creator
    FOREIGN KEY (creator_id) REFERENCES app_user (id),
  CONSTRAINT fk_behavior_record_antecedent
    FOREIGN KEY (antecedent_code) REFERENCES antecedent_type (code),
  CONSTRAINT fk_behavior_record_behavior
    FOREIGN KEY (behavior_code) REFERENCES behavior_type (code),
  CONSTRAINT fk_behavior_record_consequence
    FOREIGN KEY (consequence_code) REFERENCES consequence_type (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_record_assistance (
  behavior_record_id BIGINT UNSIGNED NOT NULL,
  assistance_code VARCHAR(64) NOT NULL,
  PRIMARY KEY (behavior_record_id, assistance_code),
  KEY idx_behavior_record_assistance_type (assistance_code),
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

CREATE TABLE IF NOT EXISTS student_training_plan (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  student_id BIGINT UNSIGNED NOT NULL,
  training_context VARCHAR(128) NOT NULL,
  training_goal VARCHAR(255) NOT NULL,
  initial_level VARCHAR(32) NULL,
  current_level VARCHAR(32) NULL,
  current_performance VARCHAR(1000) NULL,
  plan_phase VARCHAR(64) NULL,
  status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'PAUSED') NOT NULL,
  teacher_remark VARCHAR(1000) NULL,
  PRIMARY KEY (id),
  KEY idx_student_training_plan_student_status (student_id, status),
  CONSTRAINT fk_student_training_plan_student
    FOREIGN KEY (student_id) REFERENCES student (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS teacher_evaluation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  student_id BIGINT UNSIGNED NOT NULL,
  teacher_id BIGINT UNSIGNED NOT NULL,
  period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  evaluation_text VARCHAR(2000) NULL,
  PRIMARY KEY (id),
  KEY idx_teacher_evaluation_student_period (student_id, period_type, period_start, period_end),
  KEY idx_teacher_evaluation_teacher (teacher_id),
  CONSTRAINT chk_teacher_evaluation_period CHECK (period_start <= period_end),
  CONSTRAINT fk_teacher_evaluation_student
    FOREIGN KEY (student_id) REFERENCES student (id),
  CONSTRAINT fk_teacher_evaluation_teacher
    FOREIGN KEY (teacher_id) REFERENCES app_user (id)
) ENGINE=InnoDB;
