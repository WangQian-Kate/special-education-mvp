# 成员 B 核心接口与 Apifox 设计（任务 3）

## 1. 文档目的

本文档定义成员 B 当前阶段负责的核心 REST API，并作为 `backend/docs/openapi.yaml` 和 Apifox 项目的设计依据。

本任务只确定接口契约，不修改 Java 业务代码、SQL 脚本或正式 MySQL 数据库。后续任务必须以本接口契约为准实现和测试。

## 2. 需求优先级

接口设计按以下顺序确定：

1. 用户在当前对话中已经确认的决定；
2. `7.15-7.16任务.md` 中成员 B 的任务；
3. 前端提供的《产品流程与前端设计》；
4. 已验收的数据模型和后端架构；
5. 共享对话仅作参考；
6. 现有接口代码和旧 OpenAPI 只作为差异检查对象，不作为最终需求。

## 3. 已确认的接口原则

- 保留任务文件指定的三个核心路径：`POST /behavior`、`GET /behavior/{studentId}`、`GET /statistics/{studentId}`。
- 当前不添加 `/api/v1` 前缀。
- 为支持前端已经明确提出的观察信息实时保存、详细记录编辑和删除，增加观察周期和记录维护接口。
- 快速记录只要求 B 行为；A 前因和 C 后果允许为空。
- 前端发送稳定 code，中文 label 由后端查询字典后返回。
- `scene` 与前端“环境”含义相同，统一使用 `environmentCode`，不再保留 `scene`。
- 快速点击一次创建一条行为记录，`frequency` 默认值为 `1`。
- 辅助方式使用 code 数组表示多选。
- 成功响应直接返回资源数据；失败响应统一返回 `code` 和 `message`。
- 当前删除采用直接删除，不增加未确认的软删除字段。
- 当前不设计微信登录、认证、AI 分析、PDF 导出和离线同步接口。

## 4. 通用约定

### 4.1 服务地址

本地开发地址：

```text
http://127.0.0.1:8080
```

当前没有认证请求头。未来接入微信登录或其他认证时另行扩展，不在本任务中预留虚假令牌。

### 4.2 数据格式

- 请求和响应使用 `application/json`。
- JSON 字段使用 `lowerCamelCase`。
- ID 使用正整数，对应 Java `Long` 和 MySQL `BIGINT UNSIGNED`。
- 日期使用 `YYYY-MM-DD`。
- 时间使用带时区偏移的 ISO 8601 格式，例如 `2026-07-16T09:23:00+08:00`。
- code 使用大写下划线形式，例如 `CLASSROOM`、`LEAVE_SEAT`。
- 百分比取值范围为 `0` 到 `100`。

### 4.3 排序规则

- 行为记录按 `recordTime` 倒序排列；时间相同时按记录 ID 倒序排列。
- 行为统计按频次倒序排列；频次相同时按 `behaviorCode` 升序排列。
- 趋势数据按日期升序排列。
- 环境分布按频次倒序排列。

### 4.4 空值与 PATCH 规则

- 创建请求中未提供的可选字段保存为空。
- PATCH 请求中未出现的字段保持原值。
- PATCH 请求中显式传入 `null` 表示清空可空字段。
- `assistanceCodes` 未出现时保持原值；传入空数组时清空全部辅助方式。
- PATCH 请求至少需要包含一个可修改字段。

## 5. 接口清单

| 模块 | 方法 | 路径 | 用途 | 来源 |
|---|---|---|---|---|
| 系统 | GET | `/health` | 服务健康检查 | 现有技术接口 |
| 观察周期 | POST | `/observation-session` | 创建观察周期 | 前端观察信息、已验收数据模型 |
| 观察周期 | GET | `/observation-session/{sessionId}` | 查询一个观察周期 | 前端恢复当前观察信息 |
| 观察周期 | PATCH | `/observation-session/{sessionId}` | 实时更新观察信息和本周期行为备注 | 前端实时保存 |
| 行为记录 | POST | `/behavior` | 新增快速或详细行为记录 | 任务文件指定 |
| 行为记录 | GET | `/behavior/{studentId}` | 查询学生行为记录 | 任务文件指定 |
| 行为记录 | PATCH | `/behavior/records/{recordId}` | 补充或编辑行为详细记录 | 前端明确支持编辑 |
| 行为记录 | DELETE | `/behavior/records/{recordId}` | 删除行为记录 | 前端明确支持删除和次数减少 |
| 行为统计 | GET | `/statistics/{studentId}` | 获取频次、占比、趋势和环境分布 | 任务文件及前端图表 |

除健康检查外，共有 8 个当前业务接口。

## 6. 观察周期接口

### 6.1 创建观察周期

```http
POST /observation-session
```

请求字段：

| 字段 | 必填 | 说明 |
|---|---|---|
| `studentId` | 是 | 当前被观察学生 ID |
| `creatorId` | 是 | 当前记录教师 ID |
| `observationDate` | 是 | 观察日期 |
| `courseCode` | 是 | 课程 code |
| `courseOtherDescription` | 条件必填 | `courseCode` 为 `OTHER` 时填写 |
| `environmentCode` | 是 | 环境 code |
| `environmentOtherDescription` | 条件必填 | `environmentCode` 为 `OTHER` 时填写 |
| `observationDurationMinutes` | 是 | 观察周期分钟数，必须大于 0 |
| `periodBehaviorRemark` | 否 | 本周期行为备注，最多 1000 字符 |

示例：

```json
{
  "studentId": 1,
  "creatorId": 1,
  "observationDate": "2026-07-16",
  "courseCode": "CHINESE",
  "environmentCode": "CLASSROOM",
  "observationDurationMinutes": 40,
  "periodBehaviorRemark": "下午注意力有所下降。"
}
```

成功返回 `201 Created` 和完整观察周期对象，响应中包含不可修改的 `studentId`。

### 6.2 查询观察周期

```http
GET /observation-session/{sessionId}
```

查询成功返回课程和环境的 code、中文 label，以及观察时长和本周期行为备注。不存在时返回 `404`。

### 6.3 更新观察周期

```http
PATCH /observation-session/{sessionId}
```

允许修改：

- `observationDate`
- `courseCode`
- `courseOtherDescription`
- `environmentCode`
- `environmentOtherDescription`
- `observationDurationMinutes`
- `periodBehaviorRemark`

该接口供前端对顶部观察信息和本周期行为备注执行实时保存。`studentId` 和 `creatorId` 创建后不允许修改。

课程或环境从 `OTHER` 改为普通 code 时，后端自动清空对应的其他说明。

## 7. 行为记录接口

### 7.1 新增行为记录

```http
POST /behavior
```

创建请求字段：

| 字段 | 快速记录 | 详细记录 | 说明 |
|---|---|---|---|
| `observationSessionId` | 必填 | 必填 | 所属观察周期 |
| `studentId` | 必填 | 必填 | 当前学生 |
| `creatorId` | 必填 | 必填 | 记录教师 |
| `recordTime` | 可省略 | 可填写 | 省略时由服务器记录当前时间 |
| `antecedentCode` | 可空 | 可填写 | A 前因 code |
| `behaviorCode` | 必填 | 必填 | B 行为 code |
| `consequenceCode` | 可空 | 可填写 | C 后果 code |
| `assistanceCodes` | 可空 | 可多选 | 辅助方式 code 数组 |
| `assistanceResult` | 可空 | 可填写 | `SUCCESS`、`PARTIAL_SUCCESS`、`FAILED` |
| `assistanceOtherDescription` | 可空 | 条件填写 | 选择其他辅助方式时填写 |
| `frequency` | 默认 1 | 可填写 | 必须大于 0 |
| `durationSeconds` | 可空 | 可填写 | 非负整数，单位秒 |
| `remark` | 可空 | 可填写 | 详细备注，最多 500 字符 |

快速记录示例：

```json
{
  "observationSessionId": 1001,
  "studentId": 1,
  "creatorId": 1,
  "behaviorCode": "LEAVE_SEAT"
}
```

详细记录示例：

```json
{
  "observationSessionId": 1001,
  "studentId": 1,
  "creatorId": 1,
  "recordTime": "2026-07-16T09:23:00+08:00",
  "antecedentCode": "TEACHER_QUESTION",
  "behaviorCode": "LEAVE_SEAT",
  "consequenceCode": "VERBAL_PROMPT",
  "assistanceCodes": ["VERBAL_PROMPT", "GESTURE_PROMPT"],
  "assistanceResult": "PARTIAL_SUCCESS",
  "frequency": 1,
  "durationSeconds": 120,
  "remark": "语言提示后回到座位。"
}
```

成功返回 `201 Created` 和完整行为记录。前端必须保存响应中的记录 ID，供后续编辑、删除和次数减少使用。

### 7.2 查询学生行为记录

```http
GET /behavior/{studentId}
```

可选查询参数：

| 参数 | 说明 |
|---|---|
| `startDate` | 开始日期，包含当天 |
| `endDate` | 结束日期，包含当天 |
| `courseCode` | 按课程筛选 |
| `environmentCode` | 按环境筛选 |
| `observationSessionId` | 按观察周期筛选 |

`startDate` 和 `endDate` 必须同时提供，且开始日期不能晚于结束日期。全部省略时返回该学生的全部行为记录。

学生存在但没有符合条件的记录时返回 `200 OK` 和空数组；学生不存在时返回 `404`。

### 7.3 更新行为详细记录

```http
PATCH /behavior/records/{recordId}
```

允许修改：

- 发生时间；
- A 前因、B 行为、C 后果；
- 多选辅助方式；
- 辅助结果和其他辅助说明；
- 频次；
- 持续时间；
- 备注。

`studentId`、`creatorId` 和 `observationSessionId` 创建后不允许通过该接口修改。

### 7.4 删除行为记录

```http
DELETE /behavior/records/{recordId}
```

成功返回 `204 No Content`。后端实现时必须在同一事务内删除该记录对应的辅助方式关联。

前端点击次数减少按钮时，删除当前观察周期和当前行为中最近的一条快速记录。前端可以使用新增接口返回的记录 ID，或者从查询结果中选择最近记录后调用删除接口。

## 8. 行为统计接口

```http
GET /statistics/{studentId}
```

可选查询参数：

| 参数 | 说明 |
|---|---|
| `startDate` | 统计开始日期，包含当天 |
| `endDate` | 统计结束日期，包含当天 |
| `courseCode` | 按课程筛选 |
| `environmentCode` | 按环境筛选 |

前端日、周、月切换负责计算对应的 `startDate` 和 `endDate`。查询参数全部省略时统计该学生的全部行为数据。

响应包含：

- `totalFrequency`：筛选范围内所有记录 `frequency` 的总和；
- `items`：每种行为的频次和占比，用于柱状图和行为占比图；
- `trend`：按日期汇总的行为频次，用于趋势图；
- `environmentDistribution`：按环境汇总的频次和占比。

无记录时返回 `200 OK`，`totalFrequency` 为 `0`，三个数组均为空。

统计字段不得保存成独立统计副本，必须由行为记录实时聚合得到。

## 9. 响应结构

### 9.1 成功响应

成功时直接返回对应资源或数组，不额外包裹 `code`、`message`、`data`。

常用状态码：

| 状态码 | 使用场景 |
|---|---|
| `200 OK` | 查询或更新成功 |
| `201 Created` | 创建成功 |
| `204 No Content` | 删除成功 |

### 9.2 失败响应

所有错误使用：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "请求参数不合法"
}
```

当前错误码：

| HTTP 状态 | `code` | 场景 |
|---|---|---|
| `400` | `VALIDATION_ERROR` | 格式、范围、条件必填或字典 code 错误 |
| `404` | `RESOURCE_NOT_FOUND` | 学生、教师、观察周期或行为记录不存在 |
| `500` | `INTERNAL_ERROR` | 未预期服务异常 |

错误信息不得包含数据库密码、SQL、堆栈或本机路径。

## 10. 关键业务校验

1. 观察周期、学生和记录人必须存在。
2. 记录人必须与学生存在有效绑定关系。
3. 课程、环境、A/B/C 和辅助方式必须使用有效字典 code。
4. B 行为必填；A 和 C 允许为空。
5. 课程或环境为 `OTHER` 时，对应说明必填。
6. 辅助方式包含 `OTHER` 时，`assistanceOtherDescription` 必填。
7. 课程、环境或辅助方式不再使用 `OTHER` 时，自动清空对应的其他说明。
8. `frequency` 必须大于 0。
9. `durationSeconds` 必须大于或等于 0。
10. 日期范围必须同时提供开始和结束日期，开始日期不能晚于结束日期。
11. 删除不存在的记录返回 `404`，不把重复删除伪装成成功。

## 11. 与数据模型的对应关系

| API 内容 | 数据来源 |
|---|---|
| 观察日期、课程、环境、时长、本周期行为备注 | `observation_session` |
| A、B、C、频次、持续时间、详细备注 | `behavior_record` |
| 多选辅助方式 | `behavior_record_assistance` |
| 课程和环境中文名称 | `course_type`、`environment_type` |
| A/B/C 中文名称 | `antecedent_type`、`behavior_type`、`consequence_type` |
| 辅助方式中文名称 | `assistance_type` |
| 统计结果 | 对 `behavior_record` 和观察周期实时聚合 |

## 12. 任务 6 实施前的代码差异

下表记录任务 3 设计接口时的旧代码差异，所列目标已在任务 6 的接口框架与持久层适配中完成。

| 当前实现 | 本任务确定的目标 |
|---|---|
| A、B、C 全部必填 | 只有 B 必填，A/C 可空 |
| 使用 `scene` | 改为观察周期中的 `environmentCode` |
| 行为记录没有观察周期 | 新增 `observationSessionId` |
| 没有辅助方式、结果、频次和持续时间 | 按最终请求和响应补全 |
| 查询不支持筛选 | 增加日期、课程、环境和观察周期筛选 |
| 统计只有行为次数 | 增加占比、日期趋势和环境分布 |
| 没有观察周期接口 | 增加创建、查询和更新接口 |
| 没有编辑和删除接口 | 增加 PATCH 和 DELETE 接口 |
| 现有错误响应内容有限 | 统一 `code + message` |

以上差异在后续项目分层整改和接口框架任务中实现，本任务不直接修改 Java 代码。

## 13. Apifox 交付要求

- 使用 OpenAPI 3.0.3。
- 接口按“系统、观察周期、行为记录、行为统计”分组。
- 每个接口具有中文名称、用途、参数说明、示例和状态码。
- 快速记录和详细记录分别提供请求示例。
- 所有失败响应引用统一错误结构。
- 导入文件必须能够被 Apifox 识别，不依赖本机绝对路径。

对应文件：`backend/docs/openapi.yaml`。

## 14. 需要与前端沟通的事项

以下内容不阻塞当前任务，但在正式联调前需要前端确认：

- 前端请求使用 code，界面显示使用后端返回的 label；
- 创建观察周期时必须发送当前 `studentId`；
- 快速记录后保存记录 ID，用于减少、编辑和删除；
- 查询参数采用 `startDate`、`endDate`、`courseCode`、`environmentCode` 和 `observationSessionId`；
- 成功响应不使用统一外层包装，失败响应使用 `code + message`；
- 柱状图、占比图、趋势图和环境分布图分别使用统计响应中的对应数组；
- 当前不提供离线批量同步接口。

## 15. 本任务边界

本任务不包含：

- Java Controller、Service、Mapper、Entity 或 DTO 修改；
- SQL 脚本和正式数据库修改；
- 训练计划、“我的”和教师评价接口实现；
- 微信登录、认证和权限控制；
- AI 分析、PDF 导出或分享；
- 分页、离线同步和软删除；
- README 修改。
