# 训练计划页面 API

## 接口

所有数据均属于 Bearer 会话对应教师的当前学生。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/training-plan/categories` | 查询标准目标分类 |
| GET | `/training-plan/library` | 搜索和筛选 165 项标准目标库 |
| GET | `/training-plan/items` | 查询当前学生已分配目标 |
| GET | `/training-plan/goals-progress` | 查询全部已激活目标的周/月进度 |
| GET | `/training-plan/goals/{standardNumber}/records` | 查询标准目标关联的最新行为记录 |
| POST | `/training-plan/items/standard` | 激活标准目标 |
| POST | `/training-plan/items/custom` | 创建自定义目标 |
| PATCH | `/training-plan/items/{itemId}` | 即时更新当前等级、阶段或状态 |
| DELETE | `/training-plan/items/{itemId}` | 取消分配或删除自定义目标 |

## 目标库和激活

- 系统内置 10 个标准分类、165 项标准目标，另有 `CUSTOM` 自定义分类。
- `standardNumber` 是稳定业务编号 `1-165`，不等于数据库主键。
- 激活标准目标必须提供学期初等级；当前等级默认相同，阶段默认 `1`，状态默认 `NOT_STARTED`。
- 同一标准目标不能重复分配给同一学生；自定义目标只属于当前学生。

## 目标进度

- `WEEKLY` 返回周期合计；`MONTHLY` 额外返回 4–6 个自然周计数。
- 返回当前学生全部已激活目标，无记录目标的全部次数为 0。
- 每条行为记录按 `behaviorCode` 计入该主行为关联的全部标准目标，不按子行为二次过滤。
- 自定义目标当前没有标准行为映射，因此统计次数为 0。
- 返回 `unclassifiedCount`，快速记录未选择三态时不被误算为未完成。

## 目标关联记录

- 按发生时间倒序返回，`limit` 默认 10、最大 50；`totalCount` 是截断前总数。
- `subBehaviors` 和 `performanceOptions` 均为数组。
- `performanceText` 优先使用“其它”选项的 `customText`，多项用中文逗号拼接，无选项时为“未填写”。
- 不存在的 `standardNumber` 返回 HTTP 404。

## 即时更新和删除

只允许修改 `currentLevel`、`phase`（1–3）、`status`（`NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`PAUSED`），`initialLevel` 只读。已产生进度时首次删除返回 409/`40902`，前端确认后用 `confirmed=true` 重试。

等级全集及含义仍待提供。
