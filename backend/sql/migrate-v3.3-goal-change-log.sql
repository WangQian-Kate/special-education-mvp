-- migrate-v3.3: 训练目标变更日志表
CREATE TABLE IF NOT EXISTS training_goal_change_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  student_goal_id BIGINT UNSIGNED NOT NULL,
  standard_number SMALLINT UNSIGNED NULL,
  goal_text VARCHAR(500) NULL,
  changed_by BIGINT UNSIGNED NOT NULL,
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  field_name VARCHAR(32) NOT NULL COMMENT 'current_level/phase/status',
  old_value VARCHAR(64) NULL,
  new_value VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_goal_log_goal (student_goal_id),
  KEY idx_goal_log_standard (standard_number),
  CONSTRAINT fk_goal_log_goal FOREIGN KEY (student_goal_id) REFERENCES student_training_goal(id) ON DELETE CASCADE,
  CONSTRAINT fk_goal_log_user FOREIGN KEY (changed_by) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
