# 训练计划页面 API

## 页面范围

本目录覆盖标准目标库、学生目标分配、自定义目标、搜索筛选和表格内即时更新。

所有数据均属于 `X-User-Id` 对应的当前学生。

## 标准目标分类

| code | 名称 | 目标数 | 编号范围 |
|---|---|---:|---|
| `SCHOOL_CLASS_AWARENESS` | 学校/班级意识 | 10 | 1-10 |
| `SCHOOL_ENTRY_KNOWLEDGE` | 入校常识 | 12 | 11-22 |
| `SCHOOL_LEAVING_ROUTINE` | 离校常规 | 10 | 23-32 |
| `SPORTS` | 运动 | 22 | 33-54 |
| `EXERCISES` | 做操 | 10 | 55-64 |
| `STAIRS` | 上下楼梯 | 11 | 65-75 |
| `QUEUE` | 排队 | 12 | 76-87 |
| `DINING` | 用餐 | 23 | 88-110 |
| `BREAK_TIME` | 课间休息 | 20 | 111-130 |
| `GROUP_CLASS` | 集体课 | 35 | 131-165 |

另有 `CUSTOM` 自定义分类，不属于 165 项标准目标。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/training-plan/categories` | 查询标准目标分类 |
| GET | `/training-plan/library` | 搜索和筛选 165 项标准目标库 |
| GET | `/training-plan/items` | 查询当前学生已分配目标 |
| POST | `/training-plan/items/standard` | 激活标准目标 |
| POST | `/training-plan/items/custom` | 创建自定义目标 |
| PATCH | `/training-plan/items/{itemId}` | 即时更新当前等级、阶段或状态 |
| DELETE | `/training-plan/items/{itemId}` | 取消分配或删除自定义目标 |

## 激活规则

- 系统内置 10 个标准分类、165 项标准目标，另有“自定义”分类。
- `standardNumber` 是目标的稳定业务编号 `1-165`，不等同于数据库主键 `id`。
- 激活标准目标时必须提供学期初等级。
- 当前等级默认等于学期初等级。
- 阶段默认 `1`。
- 状态默认 `NOT_STARTED`。
- 同一标准目标不能重复分配给同一学生。

## 自定义目标

- 自定义目标统一归入“自定义”分类。
- 自定义目标只属于当前学生。
- 删除时同时永久删除该自定义目标。

## 即时更新

列表行不进入详情页，只允许更新：

- `currentLevel`
- `phase`：`1`、`2`、`3`
- `status`：`NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`PAUSED`

`initialLevel` 只读。

## 删除确认

满足任一条件即视为已经产生进度：

- 当前等级与学期初等级不同。
- 阶段不为 `1`。
- 状态不为 `NOT_STARTED`。

首次删除返回 `409 PROGRESS_CONFIRMATION_REQUIRED`；前端确认后使用 `confirmed=true` 重试。

## 待提供数据

- 等级全集和含义。
