# 随班记录页面 API

## 页面范围

本目录覆盖日记录的课堂信息、行为卡片、快速记录、补记、详细记录和删除，以及周记录、月记录的只读汇总。

所有正式请求均使用 Bearer 会话确定当前教师，并从“我的”模块读取当前学生。

课程固定为语文、数学、英语、体育、音乐、美术、科学、道法、体能、个训、课间、午餐、午休、自习、其他和全天汇总。环境固定为普通教室、资源教室、操场、楼道、卫生间、校外和其他；稳定 code 见 `../../数据字典.md`。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/class-records` | 查询指定日期的课堂记录列表 |
| POST | `/class-records` | 新建一次独立课堂记录 |
| GET | `/class-records/{classRecordId}` | 查询顶部信息和行为卡片次数 |
| PATCH | `/class-records/{classRecordId}` | 自动保存日期、课程、环境、时长和整体备注 |
| GET | `/class-records/behavior-options` | 查询课程/环境对应的标准行为目录 |
| GET | `/class-records/abc-tags` | 查询 A/C 分组快捷标签字典 |
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
- 主页面只显示次数，次数等于当前课堂记录下该行为各记录 `occurrenceCount` 之和。
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
- A（行为前因）和 C（行为后果）使用服务端受控的客观事实快捷标签；B 使用主行为、本次发生次数、可空持续时间及可选事实描述。
- `GET /class-records/abc-tags` 分别返回 `antecedentGroups` 和 `consequenceGroups`。前端按 `displayOrder` 和分组渲染，可自行用颜色或间距区分大类。
- 保存时分别提交 `antecedentSelections` 和 `consequenceSelections`，数组元素至少包含稳定 `code`。任一标签均可附带可选 `customText`，补充文本不会生成新的字典项。
- 原有 `antecedentText`、`consequenceText` 保留为可选补充文本，以兼容旧数据和临时描述；快捷标签和自由文本可以同时保存。
- 后端会把所选标签中文名称、可选客观补充内容和补充文本合并后提供给 AI 分析。
- 主行为固定为 `B001-B110`，目录接口同时返回模块、分组及状态栏、推荐课程/环境、训练目标、子行为和表现/状态选项。
- 子行为与行为表现支持多选；表现选项只能保存到所属主行为，隶属子行为的状态必须同时选择对应子行为。
- `requiresCustomText = true` 的“其它”状态必须填写 `customText`，其余状态禁止附加自由文本。
- 行为功能固定使用 `ATTENTION`、`TANGIBLE`、`ESCAPE`、`SENSORY`。
- 辅助方式支持多选，每个选中项只要求稳定 code，补充内容可不填写。
- 辅助结果为自由文本。
- `occurrenceCount` 表示本次事件内部重复次数，默认 1；`durationSeconds` 为可空秒级持续时间。
- 本次次数、持续时间、行为环节、A/C 快捷标签、A/B/C 文本、行为功能、辅助方式、辅助结果、子行为或行为表现任意一项有值即可保存；全部为空时拒绝。
- 只要成功执行过一次保存，`detailSaved` 永久为 `true`。

## 待提供选项

- 行为环节
