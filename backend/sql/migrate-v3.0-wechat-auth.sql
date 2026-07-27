-- V3.0.0：接入微信 code2Session、教师首次绑定和 Bearer 登录会话。
-- 本迁移只创建认证结构，不写入 AppSecret、OpenID、原始 token 或明文绑定码。

USE special_ed_assistant;

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

INSERT IGNORE INTO schema_migration (version) VALUES ('3.0.0');
