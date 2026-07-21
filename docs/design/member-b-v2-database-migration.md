# 成员 B V2 数据库迁移说明

## 1. 当前结论

V2 数据库脚本已通过 MySQL 8.0.45 临时数据库演练，并于 2026-07-17 成功迁移正式本地数据库 `special_ed_assistant`。

迁移完成后的状态：

| 项目 | 结果 |
|---|---|
| 数据库版本 | `2.4.0` |
| 表总数 | 18 |
| 原用户、学生和绑定关系 | 已保留 |
| 当前学生 | 已初始化 |
| 旧业务表 | 已移除 |
| V2 Java 数据库集成测试 | 通过 |
| 标准训练目标 | 10 个标准分类、165 项，编号连续且唯一 |
| 白名单身份 | 3 名教师、6 名学生及绑定关系 |

迁移前已生成数据库备份：

```text
C:\Users\Asus\Downloads\special_ed_assistant-v03-backup-20260717-0125.sql
C:\Users\Asus\Downloads\special_ed_assistant-v20-before-goals-20260717-022338.sql
C:\Users\Asus\Downloads\special_ed_assistant-v21-before-whitelist-20260717-143813.sql
```

## 2. 文件用途

| 文件 | 使用场景 | 是否可重复执行 |
|---|---|---|
| `backend/sql/schema.sql` | 全新数据库创建 V2 结构 | 是 |
| `backend/sql/seed.sql` | 本地开发基础字典和演示用户 | 是 |
| `backend/sql/migrate-v2.sql` | 已存在 V0.3 数据库升级到 V2 | 是，成功后由 `2.0.0` 版本标记跳过 |
| `backend/sql/migrate-v2.1-training-goals.sql` | V2.0.0 增加标准目标业务编号结构 | 是 |
| `backend/sql/migrate-v2.2-whitelist-identities.sql` | V2.1.0 增加白名单教师、学生外部编号和性别 | 是 |
| `backend/sql/migrate-v2.3-assistance-validation.sql` | V2.2.0 替换辅助方式字典并允许辅助内容为空 | 是 |

`seed.sql` 是本地开发数据，会更新 ID 为 `1` 的演示用户和学生。生产数据环境不能直接执行种子文件。

## 3. 迁移保护

`migrate-v2.sql` 在执行删除操作前统计以下旧业务表：

- `observation_session`
- `behavior_record`
- `student_training_plan`
- `teacher_evaluation`

只要任一旧业务表存在数据，迁移就以 `SQLSTATE 45000` 中止并保留原表。脚本不会猜测旧数据如何映射到新模型，因为旧模型中的频次、A/C code、辅助结果枚举和训练计划单表字段无法无损转换为已确认的新交互。

本次正式库的旧业务表均为空，满足受保护迁移条件。

## 4. 执行方式

### 4.1 全新本地数据库

进入 MySQL 客户端后执行：

```sql
source C:/项目绝对路径/backend/sql/schema.sql;
source C:/项目绝对路径/backend/sql/seed.sql;
```

### 4.2 已存在的 V0.3 本地数据库

先备份，再进入 MySQL 客户端执行：

```sql
source C:/项目绝对路径/backend/sql/migrate-v2.sql;
source C:/项目绝对路径/backend/sql/seed.sql;
```

如果迁移提示旧业务表非空，必须单独制定数据转换方案，不能绕过保护直接删表。

### 4.3 已存在的 V2.0.0 本地数据库

先备份，再依次执行：

```sql
source C:/项目绝对路径/backend/sql/migrate-v2.1-training-goals.sql;
source C:/项目绝对路径/backend/sql/seed.sql;
```

第一步增加可空且唯一的 `standard_number`；第二步幂等写入 10 个标准分类、165 项目标和 `2.1.0` 版本记录。

### 4.4 已存在的 V2.1.0 本地数据库

先备份，再执行：

```sql
source C:/项目绝对路径/backend/sql/migrate-v2.2-whitelist-identities.sql;
```

脚本幂等增加 `teacher_id`、`student_code` 和 `gender`，并写入 3 名教师、6 名学生、绑定关系、默认当前学生和 `2.2.0` 版本记录。

### 4.5 已存在的 V2.2.0 本地数据库

先备份，再执行：

```sql
source C:/项目绝对路径/backend/sql/migrate-v2.3-assistance-validation.sql;
```

脚本将辅助方式替换为确认后的 9 项，删除辅助内容非空检查并将 `content` 改为可空，最后写入 `2.3.0` 版本记录。若检测到仍使用旧编码的辅助明细，脚本会主动中止，避免静默丢失历史数据。

## 5. 临时数据库演练结果

| 验收项 | 结果 |
|---|---|
| 全新结构和种子脚本执行 | 通过 |
| V0.3 空业务表升级到 V2 | 通过 |
| V2 表总数为 18 | 通过 |
| 旧业务表和旧 A/C 字典表移除 | 通过 |
| 原用户、学生和绑定关系保留 | 通过 |
| 当前学生初始化 | 通过 |
| 迁移脚本重复执行 | 通过 |
| 种子脚本重复执行 | 通过 |
| 旧业务表非空时拒绝迁移 | 通过 |
| 相同学生、日期、课程和环境可创建多条课堂记录 | 通过 |
| 当前学生必须已绑定 | 通过 |
| 辅助方式内容允许为空 | 通过 |
| 同一学生的目标不可重复分配 | 通过 |
| 训练阶段限制为 1 至 3 | 通过 |
| 行为记录不存在旧 `frequency` 字段 | 通过 |
| V2.0.0 升级到 V2.1.0 | 通过 |
| 165 项编号为连续的 1-165 | 通过 |
| 目标升级和种子脚本重复执行 | 通过 |
| V2.1.0 升级到 V2.2.0 | 通过 |
| 白名单迁移脚本重复执行 | 通过 |
| V2.2.0 升级到 V2.3.0 | 通过 |
| 辅助方式迁移脚本重复执行 | 通过 |

临时数据库在演练结束后已删除。

## 6. 正式数据库验收结果

正式迁移完成后执行了以下验证：

- `schema_migration` 最新版本为 `2.4.0`。
- 数据库共有 18 张表。
- `class_record`、`behavior_record` 和 `student_training_goal` 均为空。
- `training_goal` 包含 165 项标准目标，`standard_number` 从 1 到 165 且无重复。
- 3 名白名单教师、6 名学生、师生绑定和当前学生均存在。
- `behavior_function_type` 包含 `ATTENTION`、`TANGIBLE`、`ESCAPE`、`SENSORY` 4 项。
- `migrate-v2.4-behavior-functions.sql` 在正式数据库连续执行两次均成功，随后重复执行 `seed.sql` 也成功。
- 应用账号 `special_ed_app` 可通过正式配置访问数据库。
- 6 个数据库集成测试场景全部通过；测试事务均已回滚，未污染正式业务数据。

正式迁移完成后不再运行旧 V0.3 Java 服务。
