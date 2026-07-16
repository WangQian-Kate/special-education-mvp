# 随班记录页面 API

## 页面范围

本目录覆盖日记录的课堂信息、行为卡片、快速记录、补记、详细记录和删除，以及周记录、月记录的只读汇总。

所有请求均使用 `X-User-Id` 确定当前用户，并从“我的”模块读取当前学生。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/class-records` | 查询指定日期的课堂记录列表 |
| POST | `/class-records` | 新建一次独立课堂记录 |
| GET | `/class-records/{classRecordId}` | 查询顶部信息和行为卡片次数 |
| PATCH | `/class-records/{classRecordId}` | 自动保存日期、课程、环境、时长和整体备注 |
| GET | `/class-records/behavior-options` | 查询课程对应行为卡片 |
| GET | `/class-records/summary` | 查询周或月只读汇总 |
| POST | `/class-records/{classRecordId}/behavior-records/quick` | 快速新增一次行为 |
| POST | `/class-records/{classRecordId}/behavior-records/supplement` | 补记过去时间的一次行为 |
| GET | `/class-records/{classRecordId}/behavior-records` | 查询某行为的发生时间列表 |
| GET | `/behavior-records/{recordId}` | 查询一条详细记录 |
| PUT | `/behavior-records/{recordId}/details` | 保存一条详细记录 |
| DELETE | `/behavior-records/{recordId}` | 删除一条行为记录 |

## 快速记录

- 点击一次 `+` 创建一条记录。
- 发生时间由后端按上海时区生成。
- 主页面只显示次数，次数等于当前课堂记录下该行为的记录条数。
- 接口响应返回最新记录 ID 和 `detailSaved`，供 `-` 操作使用。

## 减少次数

- `-` 删除当前行为发生时间最近的一条记录。
- 最近记录 `detailSaved = false` 时直接删除。
- 最近记录 `detailSaved = true` 时，前端先弹出确认框，确认后调用删除接口。
- 次数为 0 时前端禁用 `-`。

## 详细记录

- 顶部时间列表只显示当前课堂记录和当前行为卡片下的行为记录。
- 点击时间读取对应详情。
- `+补记` 由用户选择过去发生时间后创建一条新记录。
- A、B、C 均为自由文本。
- 辅助方式支持多选，每个选中项的内容必填。
- 辅助结果为自由文本。
- 只要执行过一次保存，`detailSaved` 永久为 `true`。

## 待提供选项

- 行为环节
- 行为功能

