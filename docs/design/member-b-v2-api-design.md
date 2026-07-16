# 成员 B V2 API 总设计

## 1. 组织原则

V2 API 按前端一级页面组织。代码实施时，每个页面建立独立包目录；接口文档也使用对应文件夹：

```text
api/
├─ classrecord/        # 随班记录
├─ trainingplan/       # 训练计划
├─ studentevaluation/  # 学生评估
├─ profile/            # 我的
└─ system/             # 系统
```

页面之间可以复用底层统计或用户上下文服务，但 Controller、DTO 和接口说明必须归入所属页面，禁止继续把统计接口混在行为 Controller 中。

## 2. 临时用户身份

登录功能确认前，除健康检查外的业务接口统一要求：

```http
X-User-Id: 1
```

后端使用该用户读取当前学生并校验师生绑定。请求体不再发送 `creatorId`。未来接入认证后，由认证上下文替代此请求头，业务 DTO 无需再次调整。

## 3. 页面与接口

### 3.1 系统

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/health` | 服务健康检查 |

### 3.2 我的

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/me` | 查询当前用户和当前学生 |
| GET | `/me/students` | 查询当前用户绑定的学生 |
| PUT | `/me/current-student` | 切换并保存当前学生 |

资料修改、学生管理、导出历史、退出登录暂不实现。使用帮助为前端静态页面。

### 3.3 随班记录

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/class-records` | 查询当前学生指定日期的课堂记录列表 |
| POST | `/class-records` | 新建一次独立课堂记录 |
| GET | `/class-records/{classRecordId}` | 查询课堂顶部信息和行为卡片次数 |
| PATCH | `/class-records/{classRecordId}` | 防抖自动保存顶部信息和整体备注 |
| GET | `/class-records/behavior-options` | 按课程查询行为卡片选项 |
| GET | `/class-records/summary` | 查询周记录或月记录只读汇总 |
| POST | `/class-records/{classRecordId}/behavior-records/quick` | 快速记录，时间由后端生成 |
| POST | `/class-records/{classRecordId}/behavior-records/supplement` | 补记过去发生的行为 |
| GET | `/class-records/{classRecordId}/behavior-records` | 查询某行为的发生时间列表 |
| GET | `/behavior-records/{recordId}` | 查询一条详细记录 |
| PUT | `/behavior-records/{recordId}/details` | 保存完整详细记录并永久标记已保存 |
| DELETE | `/behavior-records/{recordId}` | 删除一条记录 |

日记录允许新增、补记、编辑和删除；周记录、月记录只读。相同学生、日期、课程和环境允许存在多个 `classRecordId`，由前端保存当前 ID 以区分两节课。

### 3.4 学生评估

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/student-evaluation/statistics` | 查询日报、周报或月报行为频次 |

统计始终使用当前学生和随班记录数据。AI、学期报告、导出和分享暂不实现。

### 3.5 训练计划

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/training-plan/categories` | 查询训练目标分类 |
| GET | `/training-plan/library` | 搜索和筛选标准目标库 |
| GET | `/training-plan/items` | 查询当前学生已分配目标 |
| POST | `/training-plan/items/standard` | 为当前学生激活标准目标 |
| POST | `/training-plan/items/custom` | 创建并分配自定义目标 |
| PATCH | `/training-plan/items/{itemId}` | 即时保存当前等级、阶段或状态 |
| DELETE | `/training-plan/items/{itemId}` | 取消分配或删除自定义目标 |

删除已产生进度的目标时，首次请求返回 `409 PROGRESS_CONFIRMATION_REQUIRED`；前端确认后以 `confirmed=true` 再次删除。

## 4. 已删除或替代的旧接口

以下 V0.3 接口不再作为 V2 契约：

- `POST /observation-session`
- `GET /observation-session/{sessionId}`
- `PATCH /observation-session/{sessionId}`
- `POST /behavior`
- `GET /behavior/{studentId}`
- `PATCH /behavior/records/{recordId}`
- `DELETE /behavior/records/{recordId}`
- `GET /statistics/{studentId}`

旧路径在 Java 实施批次中直接移除，不保留双写或虚假兼容层。

## 5. 错误响应

失败响应保持：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "请求参数不合法"
}
```

V2 使用的稳定错误码：

| HTTP | code | 场景 |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | 字段、日期、code 或状态不合法 |
| 400 | `CURRENT_STUDENT_REQUIRED` | 当前用户尚未选择学生 |
| 404 | `RESOURCE_NOT_FOUND` | 用户、学生、课堂记录、行为或训练目标不存在 |
| 409 | `RESOURCE_CONFLICT` | 目标重复分配等冲突 |
| 409 | `PROGRESS_CONFIRMATION_REQUIRED` | 删除已有进度目标前需要确认 |
| 500 | `INTERNAL_ERROR` | 未预期服务异常 |

错误响应不得暴露 SQL、堆栈、本机路径、数据库账号或密码。

## 6. 当前批次边界

当前批次只建立 V2 设计和 OpenAPI 0.4.0，不修改 Java、SQL、正式数据库或 Apifox。设计验收后再按页面逐批实现。
