-- migrate-v3.2: 学生能力等级 + 爱好字段
-- 适用于 v3.0/v3.1 数据库升级，建库请直接用 schema.sql
ALTER TABLE student
  ADD COLUMN IF NOT EXISTS social_adaptation VARCHAR(16) NULL COMMENT '社会适应能力',
  ADD COLUMN IF NOT EXISTS self_management VARCHAR(16) NULL COMMENT '自我管理能力',
  ADD COLUMN IF NOT EXISTS cognitive_level VARCHAR(16) NULL COMMENT '认知水平',
  ADD COLUMN IF NOT EXISTS language_comprehension VARCHAR(16) NULL COMMENT '语言理解',
  ADD COLUMN IF NOT EXISTS expression_ability VARCHAR(16) NULL COMMENT '表达能力',
  ADD COLUMN IF NOT EXISTS hobbies VARCHAR(500) NULL COMMENT '爱好';
