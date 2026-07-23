# 成员 B：统计图表与训练目标进度实施报告

## 1. 已确认并实施的规则

- 行为记录统一使用 `INCOMPLETE`、`ASSISTED`、`INDEPENDENT` 三态；快速记录暂为未分类。
- 详细保存时显式状态优先，未显式选择但勾选辅助方式时自动保存为 `ASSISTED`。
- ABC 功能新增 `OTHER` 和 `functionOtherText`；A/B/C 后续下拉字典仍等待提供，本次不伪造。
- 每条行为记录计入其主行为关联的全部标准目标，不按子行为二次过滤。
- `overview.trainingGoalCount` 统计全部已发生记录覆盖的标准目标；目标进度只返回当前学生全部已激活目标。
- 周趋势固定返回周一至周日 7 天；月统计按自然周动态拆分。
- 独立率只以三态已分类记录为分母，未分类通过 `unclassifiedCount` 单独返回。
- 不存在的标准目标编号返回现有统一 404；目标记录的子行为和表现均返回数组。

## 2. 数据库与后端改动

- 数据库升级至 V2.7.0，新增 `behavior_status_type`，`behavior_record` 新增 `status_code`、`function_other_text`。
- 新增状态索引、状态外键和行为功能“其他”CHECK；行为功能字典扩展为 5 项。
- 扩展 `GET /student-evaluation/statistics`，新增三态、未分类、训练目标覆盖、每日趋势、自然周、课程和环境统计。
- 新增 ABC 分布、行为表现趋势、训练目标进度、目标关联行为记录 4 个接口。
- 目标记录批量查询子行为和表现选项，避免逐记录 N+1 查询。
- OpenAPI 升级到 0.12.0，接口数 24 → 28；MySQL 由 27 表升级为 28 表。

## 3. 验证结果

- 本地库迁移前已备份，迁移后核对版本、3 项状态、5 项功能、索引和外键。
- 8 个数据库集成测试全部通过，新增测试覆盖状态优先级、自动辅助状态、ABC“其他”、7 天趋势、自然周拆分、目标进度和目标记录。
- 测试使用事务回滚，完成后 `class_record`、`behavior_record`、`student_training_goal` 仍为 0，不污染开发数据。
- 使用 1 万条临时行为记录执行 `EXPLAIN ANALYZE`：概览主聚合约 4.3ms、训练目标覆盖子查询约 49.9ms、逐行为三态约 48.9ms、已激活目标进度约 14.1ms、B 字段 TOP N 约 29.0ms。
- `class_record(student_id, record_date)`、`behavior_record(class_record_id, behavior_code, occurred_at, id)`、`behavior_training_goal(standard_number)` 等关键索引均被执行计划命中；当前无需增加重复索引。
- 性能数据使用固定临时主键写入并已全部清理，验收后 3 张业务表再次核对为 0 行。
- 新代码在独立端口完成真实 HTTP 验证：4 个新增/扩展聚合接口均返回 `code=0`，连接预热后周统计连续响应约 11–12ms。
- 已重新打包并替换本机 3000 端口旧实例；最终 `GET /api/health` 返回 `UP`，周统计返回 7 项 `dailyTrends`。

## 4. 需要与前端沟通

- 详细记录提交和读取统一使用大写 `statusCode`；快速记录可能返回 `null`。
- 独立率不要把 `unclassifiedCount` 计入分母，也不要再使用固定比例估算。
- 周图直接使用 `dailyTrends` 7 项；月图使用后端返回的动态自然周数组，不写死 4 周。
- 训练目标覆盖数与“已激活目标数”含义不同：前者来自已发生记录，后者由目标进度接口返回。
- ABC 功能和状态展示文本从后端返回；选择 `OTHER` 时显示并提交自定义文本框。
- 目标记录的 `subBehaviors` 已改为数组，`performanceText` 可直接展示。
- Apifox 重新导入 `backend/docs/openapi.yaml` 0.12.0，保留全局 `X-Teacher-Id`。
