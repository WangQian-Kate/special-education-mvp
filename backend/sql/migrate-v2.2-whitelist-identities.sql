-- 从 V2.1.0 升级白名单教师身份和学生外部编号结构。

USE special_ed_assistant;

DROP PROCEDURE IF EXISTS migrate_whitelist_identities;

DELIMITER $$

CREATE PROCEDURE migrate_whitelist_identities()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'app_user'
      AND column_name = 'teacher_id'
  ) THEN
    ALTER TABLE app_user
      ADD COLUMN teacher_id VARCHAR(16) NULL AFTER id;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'app_user'
      AND index_name = 'uk_app_user_teacher_id'
  ) THEN
    ALTER TABLE app_user
      ADD UNIQUE KEY uk_app_user_teacher_id (teacher_id);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'student'
      AND column_name = 'student_code'
  ) THEN
    ALTER TABLE student
      ADD COLUMN student_code VARCHAR(16) NULL AFTER id;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'student'
      AND column_name = 'gender'
  ) THEN
    ALTER TABLE student
      ADD COLUMN gender ENUM('MALE', 'FEMALE') NULL AFTER name;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'student'
      AND index_name = 'uk_student_student_code'
  ) THEN
    ALTER TABLE student
      ADD UNIQUE KEY uk_student_student_code (student_code);
  END IF;
END$$

DELIMITER ;

CALL migrate_whitelist_identities();
DROP PROCEDURE migrate_whitelist_identities;

INSERT INTO app_user (id, teacher_id, avatar, name, school, position, role) VALUES
  (1, 't001', NULL, '张老师', 'XX市特殊教育学校', '特教教师', 'RESOURCE_TEACHER'),
  (2, 't002', NULL, '王老师', 'XX市特殊教育学校', '康复师', 'RESOURCE_TEACHER'),
  (3, 't003', NULL, '李老师', 'XX市随班就读试点小学', '班主任', 'RESOURCE_TEACHER')
AS new
ON DUPLICATE KEY UPDATE
  teacher_id = new.teacher_id,
  avatar = new.avatar,
  name = new.name,
  school = new.school,
  position = new.position,
  role = new.role;

INSERT INTO student (
  id, student_code, name, gender, age, class_name, disability_type, support_goal, remark
) VALUES
  (1, 's001', '小明', 'MALE', 8, '二年级1班', '自闭症谱系障碍', '提升课堂任务持续参与能力', '仅用于本地开发'),
  (2, 's002', '小华', 'MALE', 9, '三年级2班', NULL, NULL, '仅用于本地开发'),
  (3, 's003', '小红', 'FEMALE', 7, '一年级1班', NULL, NULL, '仅用于本地开发'),
  (4, 's004', '小丽', 'FEMALE', 9, '三年级1班', NULL, NULL, '仅用于本地开发'),
  (5, 's005', '小刚', 'MALE', 10, '四年级2班', NULL, NULL, '仅用于本地开发'),
  (6, 's006', '小强', 'MALE', 8, '二年级3班', NULL, NULL, '仅用于本地开发')
AS new
ON DUPLICATE KEY UPDATE
  student_code = new.student_code,
  name = new.name,
  gender = new.gender,
  age = new.age,
  class_name = new.class_name,
  disability_type = new.disability_type,
  support_goal = new.support_goal,
  remark = new.remark;

INSERT IGNORE INTO app_user_student (user_id, student_id) VALUES
  (1, 1),
  (1, 2),
  (2, 3),
  (2, 4),
  (3, 5),
  (3, 6);

INSERT INTO app_user_current_student (user_id, student_id) VALUES
  (1, 1),
  (2, 3),
  (3, 5)
AS new
ON DUPLICATE KEY UPDATE student_id = new.student_id;

ALTER TABLE student
  MODIFY COLUMN student_code VARCHAR(16) NOT NULL;

INSERT IGNORE INTO schema_migration (version) VALUES ('2.2.0');
