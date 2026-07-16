# 成员 B V2 数据模型设计

## 1. 设计依据

本设计依据 2026-07-16 至 2026-07-17 已确认的软件原型和交互规则，替代此前以旧接口为基础的数据模型。当前批次只确认设计，不修改 SQL 或正式 MySQL。

一级页面固定为：

- 随班记录
- 训练计划
- 学生评估
- 我的

系统暂不实现微信登录、认证、AI 分析、PDF 导出、分享、离线同步、学生信息管理和个人资料修改。

## 2. 核心变化

### 2.1 行为次数

一次快速记录或补记对应一条 `behavior_record`。页面次数和学生评估频次均使用记录条数计算，不再保存或修改 `frequency`。

### 2.2 快速记录和详细记录

- 快速记录由后端生成发生时间。
- 补记由前端提交过去的发生时间。
- 行为卡片类型仍使用稳定 code，例如 `LEAVE_SEAT`。
- 详细记录中的 A、B、C 是自由文本，不是字典 code。
- 用户首次执行“保存更新”后，`detail_saved` 永久为 `TRUE`；即使后来清空详细字段也不恢复。
- 删除当前行为的最近记录时，前端根据 `detail_saved` 决定是否弹出确认框。

### 2.3 辅助方式

每种辅助方式独立保存 code 和必填内容。当前已确认的方式为：

| 分组 | code | 名称 |
|---|---|---|
| 刺激内辅助 | `VISUAL_PROMPT` | 视觉提示 |
| 刺激内辅助 | `VERBAL_PROMPT` | 语言提示 |
| 刺激内辅助 | `ACTION_PROMPT` | 动作提示 |
| 刺激外辅助 | `DEMONSTRATION` | 示范 |
| 刺激外辅助 | `PHYSICAL_ASSISTANCE` | 身体辅助 |
| 刺激外辅助 | `REINFORCEMENT` | 强化 |
| 刺激外辅助 | `ASSISTIVE_DEVICE` | 辅助设备 |

辅助结果为自由文本，不再使用“成功、部分成功、失败”枚举。

### 2.4 当前学生

后端保存每个用户当前选中的学生。保存时必须验证该学生与用户已经绑定。首次使用且绑定多个学生、尚未选择当前学生时，接口返回需要选择学生的状态。

### 2.5 训练计划

训练计划拆分为标准目标库和学生分配记录：

- 系统内置 163 项标准目标，具体清单待提供。
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

### 3.2 当前学生表 `app_user_current_student`

| 字段 | 类型 | 说明 |
|---|---|---|
| `user_id` | `BIGINT UNSIGNED` | 用户 ID，主键 |
| `student_id` | `BIGINT UNSIGNED` | 当前学生 ID |

使用 `(user_id, student_id)` 联合外键关联 `app_user_student`，从数据库层保证当前学生已经与用户绑定。

### 3.3 随班记录表 `class_record`

一行表示一次独立课堂或观察记录。相同学生、日期、课程和环境允许创建多行，以支持同一天两节相同课程分别记录。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `BIGINT UNSIGNED` | 是 | 主键 |
| `student_id` | `BIGINT UNSIGNED` | 是 | 被观察学生 |
| `creator_id` | `BIGINT UNSIGNED` | 是 | 创建用户，由 `X-User-Id` 确定 |
| `record_date` | `DATE` | 是 | 记录日期 |
| `course_code` | `VARCHAR(64)` | 是 | 课程 code |
| `course_other_description` | `VARCHAR(255)` | 否 | 课程为其他时填写 |
| `environment_code` | `VARCHAR(64)` | 是 | 环境 code |
| `environment_other_description` | `VARCHAR(255)` | 否 | 环境为其他时填写 |
| `observation_duration_minutes` | `SMALLINT UNSIGNED` | 是 | 观察时长 |
| `overall_remark` | `VARCHAR(1000)` | 否 | 本节课整体情况 |

前端保存当前 `classRecordId`，用它区分同一天条件相同的两次记录。

### 3.4 行为环节字典 `behavior_stage_type`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 稳定 code，主键 |
| `label` | `VARCHAR(64)` | 中文名称 |

具体选项待前端提供，本阶段不写入虚假数据。

### 3.5 行为功能字典 `behavior_function_type`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 稳定 code，主键 |
| `label` | `VARCHAR(64)` | 中文名称 |

具体选项待前端提供，本阶段不写入虚假数据。

### 3.6 行为记录表 `behavior_record`

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

### 3.7 辅助方式字典 `assistance_type`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 稳定 code，主键 |
| `label` | `VARCHAR(64)` | 中文名称 |
| `group_code` | `ENUM('INTERNAL_STIMULUS','EXTERNAL_STIMULUS')` | 辅助分组 |
| `display_order` | `SMALLINT UNSIGNED` | 页面顺序 |

### 3.8 行为辅助方式表 `behavior_record_assistance`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `behavior_record_id` | `BIGINT UNSIGNED` | 是 | 行为记录 ID |
| `assistance_code` | `VARCHAR(64)` | 是 | 辅助方式 code |
| `content` | `VARCHAR(500)` | 是 | 该辅助方式的具体内容 |

联合主键为 `(behavior_record_id, assistance_code)`，同一记录不能重复选择同一种辅助方式。

### 3.9 课程行为配置表 `course_behavior_config`

继续用于决定随班记录页面在指定课程下显示哪些行为卡片。当前仍按课程配置，不增加学生维度。

### 3.10 训练目标分类表 `training_goal_category`

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `VARCHAR(64)` | 分类 code，主键 |
| `label` | `VARCHAR(128)` | 分类名称 |
| `display_order` | `SMALLINT UNSIGNED` | 展示顺序 |
| `is_custom` | `BOOLEAN` | 是否为自定义分类 |

### 3.11 训练目标库 `training_goal`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `BIGINT UNSIGNED` | 是 | 主键 |
| `category_code` | `VARCHAR(64)` | 是 | 所属分类 |
| `goal_text` | `VARCHAR(500)` | 是 | 目标内容 |
| `goal_type` | `ENUM('STANDARD','CUSTOM')` | 是 | 标准或自定义 |
| `owner_student_id` | `BIGINT UNSIGNED` | 否 | 自定义目标所属学生；标准目标为空 |

标准目标由系统维护，教师不能修改名称和分类。自定义目标只属于一个学生。

### 3.12 学生训练目标分配表 `student_training_goal`

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

- 163 项标准训练目标及分类清单。
- 学期初等级、当前等级的完整取值和含义。
- 行为环节选项。
- 行为功能选项。
- 春季和秋季学期固定日期范围。

