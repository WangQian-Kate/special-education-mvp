# 成员 B V2 数据模型设计

## 1. 设计依据

本设计依据 2026-07-16 至 2026-07-21 已确认的软件原型和交互规则，替代此前以旧接口为基础的数据模型。V2 SQL、受保护迁移脚本和 Java 接口已经实现；正式 MySQL 已切换到 V2.4.0。

一级页面固定为：

- 随班记录
- 训练计划
- 学生评估
- 我的

系统当前实现 `X-Teacher-Id` 白名单伪登录，暂不实现正式微信登录、token、AI 分析、PDF 导出、分享、离线同步、学生信息管理和个人资料修改。

## 2. 核心变化

### 2.1 行为次数

一次快速记录或补记对应一条 `behavior_record`。页面次数和学生评估频次均使用记录条数计算，不再保存或修改 `frequency`。

### 2.2 快速记录和详细记录

- 快速记录由后端生成发生时间。
- 补记由前端提交过去的发生时间。
- 行为卡片类型仍使用稳定 code，例如 `LEAVE_SEAT`。
- 详细记录中的 A、B、C 是自由文本，不是字典 code。
- 持续时间、行为环节、A、B、C、行为功能、辅助方式或辅助结果任意一项有值即可保存，全部为空时拒绝。
- 用户首次成功执行“保存更新”后，`detail_saved` 永久为 `TRUE`。
- 删除当前行为的最近记录时，前端根据 `detail_saved` 决定是否弹出确认框。

### 2.3 辅助方式

每种辅助方式独立保存稳定 code；补充内容可不填写。当前已确认的方式为：

| 分组 | code | 名称 |
|---|---|---|
| 刺激内辅助 | `ADD_EXTERNAL_OBJECT` | 增加外在物品 |
| 刺激内辅助 | `CHANGE_TARGET_SIZE` | 改变目标物大小 |
| 刺激外辅助 | `FULL_BODY_ASSISTANCE` | 全身体辅助 |
| 刺激外辅助 | `HALF_BODY_ASSISTANCE` | 半身辅助 |
| 刺激外辅助 | `POSTURE_ASSISTANCE` | 姿势辅助 |
| 刺激外辅助 | `POSITION_ASSISTANCE` | 位置辅助 |
| 刺激外辅助 | `VERBAL_ASSISTANCE` | 语言辅助 |
| 刺激外辅助 | `DEMONSTRATION_ASSISTANCE` | 示范辅助 |
| 刺激外辅助 | `VISUAL_ASSISTANCE` | 视觉辅助 |

辅助结果为自由文本，不再使用“成功、部分成功、失败”枚举。

### 2.4 当前学生

后端保存每个用户当前选中的学生。保存时必须验证该学生与用户已经绑定。首次使用且绑定多个学生、尚未选择当前学生时，接口返回需要选择学生的状态。

### 2.5 训练计划

训练计划拆分为标准目标库和学生分配记录：

- 系统内置用户提供的 165 项标准目标，分为 10 个标准分类。
- 标准目标使用 `standard_number` 保存稳定业务编号 `1-165`，不依赖数据库主键。
- 教师主要通过勾选标准目标为当前学生激活计划。
- 同一标准目标不能重复分配给同一学生。
- 激活时必须填写学期初等级；当前等级默认等于学期初等级。
- 阶段默认 `1`，状态默认 `NOT_STARTED`。
- 自定义目标归入“自定义”分类，只属于创建时的当前学生。

## 3. 表结构设计

### 3.1 保留表

以下表保留并继续使用：

- `app_user`
- `student`
- `app_user_student`
- `course_type`
- `environment_type`
- `behavior_type`

### 3.2 迁移版本表 `schema_migration`

这是技术表，不属于业务页面。每个已成功应用的结构版本保存一行；当前最新版本为 `2.4.0`，用于保证迁移和标准数据脚本可追踪。

### 3.3 当前学生表 `app_user_current_student`

| 字段 | 类型 | 说明 |
|---|---|---|
| `user_id` | `BIGINT UNSIGNED` | 用户 ID，主键 |
| `student_id` | `BIGINT UNSIGNED` | 当前学生 ID |

使用 `(user_id, student_id)` 联合外键关联 `app_user_student`，从数据库层保证当前学生已经与用户绑定。

### 3.4 随班记录表 `class_record`

一行表示一次独立课堂或观察记录。相同学生、日期、课程和环境允许创建多行，以支持同一天两节相同课程分别记录。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `BIGINT UNSIGNED` | 是 | 主键 |
| `student_id` | `BIGINT UNSIGNED` | 是 | 被观察学生 |
| `creator_id` | `BIGINT UNSIGNED` | 是 | 创建用户，由 `X-Teacher-Id` 白名单解析确定 |
| `record_date` | `DATE` | 是 | 记录日期 |
| `course_code` | `VARCHAR(64)` | 是 | 课程 code |
| `course_other_description` | `VARCHAR(255)` | 否 | 课程为其他时填写 |
| `environment_code` | `VARCHAR(64)` | 是 | 环境 code |
| `environment_other_description` | `VARCHAR(255)` | 否 | 环境为其他时填写 |
| `observation_duration_minutes` | `SMALLINT UNSIGNED` | 是 | 观察时长 |
| `overall_remark` | `VARCHAR(1000)` | 否 | 本节课整体情况 |

前端保存当前 `classRecordId`，用它区分同一天条件相同的两次记录。

### 3.5 行为环节字典 `behavior_stage_type`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 稳定 code，主键 |
| `label` | `VARCHAR(64)` | 中文名称 |

具体选项待前端提供，本阶段不写入虚假数据。

### 3.6 行为功能字典 `behavior_function_type`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 稳定 code，主键 |
| `label` | `VARCHAR(64)` | 中文名称 |

固定值为 `ATTENTION`（获取关注）、`TANGIBLE`（获取实物）、`ESCAPE`（逃避）、`SENSORY`（感官刺激）。

### 3.7 行为记录表 `behavior_record`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `BIGINT UNSIGNED` | 是 | 主键 |
| `class_record_id` | `BIGINT UNSIGNED` | 是 | 所属随班记录 |
| `creator_id` | `BIGINT UNSIGNED` | 是 | 记录人 |
| `occurred_at` | `DATETIME(3)` | 是 | 行为发生时间 |
| `behavior_code` | `VARCHAR(64)` | 是 | 行为卡片类型 |
| `duration_minutes` | `SMALLINT UNSIGNED` | 否 | 持续时间 |
| `stage_code` | `VARCHAR(64)` | 否 | 行为环节 |
| `antecedent_text` | `VARCHAR(1000)` | 否 | A 行为前因 |
| `behavior_description` | `VARCHAR(1000)` | 否 | B 具体行为表现 |
| `consequence_text` | `VARCHAR(1000)` | 否 | C 行为后果 |
| `function_code` | `VARCHAR(64)` | 否 | 行为功能 |
| `assistance_result_text` | `VARCHAR(1000)` | 否 | 辅助结果自由文本 |
| `detail_saved` | `BOOLEAN` | 是 | 是否执行过保存更新，默认 `FALSE` |

不再包含：

- `frequency`
- `antecedent_code`
- `consequence_code`
- `assistance_result` 枚举
- 单一的 `assistance_other_description`

### 3.8 辅助方式字典 `assistance_type`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 稳定 code，主键 |
| `label` | `VARCHAR(64)` | 中文名称 |
| `group_code` | `ENUM('INTERNAL_STIMULUS','EXTERNAL_STIMULUS')` | 辅助分组 |
| `display_order` | `SMALLINT UNSIGNED` | 页面顺序 |

### 3.9 行为辅助方式表 `behavior_record_assistance`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `behavior_record_id` | `BIGINT UNSIGNED` | 是 | 行为记录 ID |
| `assistance_code` | `VARCHAR(64)` | 是 | 辅助方式 code |
| `content` | `VARCHAR(500)` | 否 | 该辅助方式的可选补充内容 |

联合主键为 `(behavior_record_id, assistance_code)`，同一记录不能重复选择同一种辅助方式。

### 3.10 课程行为配置表 `course_behavior_config`

继续用于决定随班记录页面在指定课程下显示哪些行为卡片。当前仍按课程配置，不增加学生维度。

### 3.11 训练目标分类表 `training_goal_category`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 分类 code，主键 |
| `label` | `VARCHAR(128)` | 分类名称 |
| `display_order` | `SMALLINT UNSIGNED` | 展示顺序 |
| `is_custom` | `BOOLEAN` | 是否为自定义分类 |

### 3.12 训练目标库 `training_goal`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `BIGINT UNSIGNED` | 是 | 主键 |
| `standard_number` | `SMALLINT UNSIGNED` | 否 | 标准目标业务编号 `1-165`，自定义目标为空 |
| `category_code` | `VARCHAR(64)` | 是 | 所属分类 |
| `goal_text` | `VARCHAR(500)` | 是 | 目标内容 |
| `goal_type` | `ENUM('STANDARD','CUSTOM')` | 是 | 标准或自定义 |
| `owner_student_id` | `BIGINT UNSIGNED` | 否 | 自定义目标所属学生；标准目标为空 |

标准目标由系统维护，教师不能修改名称和分类。`standard_number` 具有唯一约束，自定义目标只属于一个学生且该字段必须为空。

### 3.13 学生训练目标分配表 `student_training_goal`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `BIGINT UNSIGNED` | 是 | 主键 |
| `student_id` | `BIGINT UNSIGNED` | 是 | 当前学生 |
| `goal_id` | `BIGINT UNSIGNED` | 是 | 训练目标 |
| `initial_level` | `VARCHAR(32)` | 是 | 学期初等级，分配后只读 |
| `current_level` | `VARCHAR(32)` | 是 | 当前等级 |
| `phase` | `TINYINT UNSIGNED` | 是 | `1`、`2` 或 `3` |
| `status` | `ENUM('NOT_STARTED','IN_PROGRESS','COMPLETED','PAUSED')` | 是 | 当前状态 |

建立 `(student_id, goal_id)` 唯一约束，阻止重复分配。

“已产生进度”满足任一条件：

- `current_level <> initial_level`
- `phase <> 1`
- `status <> 'NOT_STARTED'`

## 4. 学生评估统计

日报、周报和月报不建立统计副本，直接从 `behavior_record` 连接 `class_record` 后按记录条数实时聚合：

```text
COUNT(behavior_record.id)
```

周期规则：

- 日报：选定自然日。
- 周报：选定日期所在的周一至周日。
- 月报：选定日期所在的自然月。
- 学期报告：固定日期待确认，确认前不写入虚假配置。

## 5. 取消的旧设计

以下旧设计不再作为 V2 实现依据：

- A、C 字典表驱动的详细记录。
- 可修改的行为 `frequency`。
- 辅助结果三值枚举。
- 只保存辅助方式 code、不保存每项内容。
- 旧的 `student_training_plan` 单表模型。
- 新原型未包含的教师评价持久化表。

## 6. 待提供数据

以下内容不阻塞快速记录和基础接口结构，但对应功能完成前必须补充：

- 学期初等级、当前等级的完整取值和含义。
- 行为环节选项。
- 春季和秋季学期固定日期范围。

## 7. SQL 实现

- `backend/sql/schema.sql`：用于全新安装，创建 17 张业务或字典表和 1 张迁移版本表。
- `backend/sql/seed.sql`：本地开发种子数据，可重复执行；包含 4 项行为功能、10 个标准分类和 165 项标准目标，不会伪造尚未提供的等级或行为环节。
- `backend/sql/migrate-v2.sql`：用于旧 V0.3 数据库。只有旧业务表全部为空时才重建业务结构；检测到业务数据会中止并保留原表。
- `backend/sql/migrate-v2.1-training-goals.sql`：用于 V2.0.0 数据库，增加标准目标业务编号结构；随后执行 `seed.sql` 写入 165 项。
- `backend/sql/migrate-v2.4-behavior-functions.sql`：用于 V2.3.0 数据库，写入 4 项固定行为功能。

详细执行和验证记录见 `member-b-v2-database-migration.md`。
