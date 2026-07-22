-- 从 V2.5.0 增加《行为条目及状态栏》标准目录结构。
-- 本迁移只允许在尚无行为记录时替换旧行为字典；执行后必须继续执行 seed.sql。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_behavior_catalog;

DELIMITER $$

CREATE PROCEDURE migrate_behavior_catalog()
BEGIN
  IF EXISTS (SELECT 1 FROM behavior_record LIMIT 1) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = '检测到旧版行为业务数据，请先完成行为编码映射后再执行 V2.6 迁移';
  END IF;

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

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'behavior_catalog_metadata'
      AND column_name = 'has_sub_behaviors'
  ) THEN
    ALTER TABLE behavior_catalog_metadata
      ADD COLUMN has_sub_behaviors BOOLEAN NOT NULL DEFAULT FALSE
      AFTER display_order;
  END IF;

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

  DELETE FROM course_behavior_config;
  DELETE FROM behavior_type;
END$$

DELIMITER ;

CALL migrate_behavior_catalog();
DROP PROCEDURE migrate_behavior_catalog;

-- 下一步执行 backend/sql/seed.sql，由受控生成区写入 B001-B100 并登记 2.6.0。
