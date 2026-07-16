CREATE DATABASE IF NOT EXISTS special_ed_assistant
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE special_ed_assistant;

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  role ENUM('RESOURCE_TEACHER', 'SHADOW_TEACHER', 'PARENT', 'ADMIN') NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  age TINYINT UNSIGNED NULL,
  class_name VARCHAR(64) NULL,
  support_goal VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS antecedent_type (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_antecedent_type_code (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_type (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_behavior_type_code (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS consequence_type (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  label VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_consequence_type_code (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavior_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  student_id BIGINT UNSIGNED NOT NULL,
  creator_id BIGINT UNSIGNED NOT NULL,
  record_time DATETIME(3) NOT NULL,
  scene ENUM('CLASSROOM', 'BREAK', 'HOME', 'OTHER') NOT NULL,
  antecedent_id BIGINT UNSIGNED NOT NULL,
  behavior_id BIGINT UNSIGNED NOT NULL,
  consequence_id BIGINT UNSIGNED NOT NULL,
  remark VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_behavior_record_student_time (student_id, record_time),
  KEY idx_behavior_record_behavior (behavior_id),
  CONSTRAINT fk_behavior_record_student FOREIGN KEY (student_id) REFERENCES student (id),
  CONSTRAINT fk_behavior_record_creator FOREIGN KEY (creator_id) REFERENCES app_user (id),
  CONSTRAINT fk_behavior_record_antecedent FOREIGN KEY (antecedent_id) REFERENCES antecedent_type (id),
  CONSTRAINT fk_behavior_record_behavior FOREIGN KEY (behavior_id) REFERENCES behavior_type (id),
  CONSTRAINT fk_behavior_record_consequence FOREIGN KEY (consequence_id) REFERENCES consequence_type (id)
) ENGINE=InnoDB;
