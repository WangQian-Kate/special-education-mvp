# 成员 B 接口框架与持久层实施说明

## 1. 实施范围

本任务依据已确认的 OpenAPI 0.3.0 和正式 MySQL 数据库，实现随班记录的观察周期、行为记录和行为统计能力。

本次不实现训练计划、学生评估、“我的”、认证、微信登录、AI、PDF 或离线同步接口。

## 2. 已实现接口

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/health` | 服务健康检查 |
| POST | `/observation-session` | 创建指定学生的观察周期 |
| GET | `/observation-session/{sessionId}` | 查询观察周期 |
| PATCH | `/observation-session/{sessionId}` | 更新观察信息和本周期行为备注 |
| POST | `/behavior` | 创建快速或详细行为记录 |
| GET | `/behavior/{studentId}` | 筛选查询学生行为记录 |
| PATCH | `/behavior/records/{recordId}` | 编辑 ABC、辅助方式、频次和详情 |
| DELETE | `/behavior/records/{recordId}` | 删除行为及辅助方式关联 |
| GET | `/statistics/{studentId}` | 获取频次、占比、趋势和环境分布 |

## 3. 分层实现

接口遵循固定依赖方向：

```text
Controller → Service → Mapper XML → MySQL
```

- Controller 负责 HTTP 参数、Bean Validation 和状态码。
- Service 负责师生绑定、字典、条件字段、事务和统计规则。
- Mapper 接口只声明数据库操作，复杂 SQL 全部位于 XML。
- Entity 表达持久化和查询结果，不直接作为 API 响应。
- DTO 独立表达请求、PATCH 字段存在状态和响应结构。

Mapper 按职责拆分为：

- `ReferenceDataMapper`：师生、绑定和字典有效性。
- `ObservationSessionMapper`：观察周期持久化。
- `BehaviorMapper`：行为及多选辅助方式持久化和筛选。
- `StatisticsMapper`：实时聚合统计。

## 4. 关键业务规则

- 创建观察周期必须提供 `studentId`，并验证记录人与学生的绑定。
- `studentId`、`creatorId` 和 `observationSessionId` 创建后不可修改。
- 快速记录只要求 B 行为，A 和 C 允许为空，频次默认 1。
- 行为记录的学生必须与观察周期所属学生一致。
- 辅助方式支持多选且不允许重复 code。
- 课程、环境或辅助方式使用 `OTHER` 时必须提供对应说明。
- 不再使用 `OTHER` 时自动清空无效的其他说明。
- PATCH 能区分未传字段与显式 `null`；未传保持原值，允许为空的字段传 `null` 时清空。
- 查询日期范围包含开始日和结束日，必须成对提供且开始日期不得晚于结束日期。
- 客户端时间统一转换为上海时区写入 MySQL，响应统一返回 `+08:00`。
- 统计使用 `SUM(frequency)`，占比四舍五入保留两位小数。
- 删除行为记录和辅助方式关联在同一事务中完成。

## 5. 查询和统计

行为查询支持：

- `startDate`、`endDate`
- `courseCode`
- `environmentCode`
- `observationSessionId`

行为记录按发生时间倒序、记录 ID 倒序返回。统计结果包含：

- `totalFrequency`
- 按行为汇总的 `items`
- 按日期升序的 `trend`
- 按环境汇总的 `environmentDistribution`

统计结果实时聚合，不建立统计副本。

## 6. 测试策略

- Controller 测试验证请求结构、状态码、错误码、查询参数和 PATCH 空对象。
- Service 测试验证默认频次、时区、观察周期归属、条件说明清理和百分比。
- Mapper 集成测试使用本机 `special_ed_app` 账号连接正式 MySQL，覆盖观察周期、行为、辅助方式、筛选、统计、更新和删除。
- Mapper 集成测试由事务自动回滚，不留下测试数据。
- 没有 `DB_PASSWORD` 环境变量时跳过真实数据库集成测试，其他测试仍可运行。

## 7. 验收结果

- 单元测试和真实 MySQL 集成测试共 23 项，全部通过。
- Spring Boot 使用最小权限应用账号启动成功。
- 真实 HTTP 验收完成观察周期创建与更新、快速记录、详细补充、筛选、统计和删除。
- 快速记录默认频次为 1，详细更新后频次为 3，两种辅助方式均正确返回。
- API 时间返回 `+08:00`，显式 `null` 成功清空本周期行为备注。
- 删除行为后查询为空，HTTP 验收数据及 Mapper 测试数据均已清理或回滚。
- OpenAPI 已升级为 0.3.0，增加观察周期必填 `studentId`。

## 8. 当前边界与后续沟通

- 当前没有认证，`creatorId` 仍由前端请求提供；接入登录后应由服务端身份确定。
- 当前接口不分页，数据量增长后需要为历史记录增加分页。
- 当前没有 `updated_at` 或版本字段，不处理多人同时编辑冲突。
- 前端必须保存新增行为返回的记录 ID。
- 前端需要重新导入 OpenAPI 0.3.0，并在创建观察周期时发送 `studentId`。
- 字典目前由数据库维护；是否增加字典查询接口仍需与前端确认。
