# 学生评估页面 API

## 页面范围

统计对象始终是 `X-Teacher-Id` 对应教师的当前学生，数据全部来自随班记录，不使用前端静态估算。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/student-evaluation/statistics` | 日、周、月概览，行为环比、三态、每日/自然周、课程和环境统计 |
| GET | `/student-evaluation/abc-distribution` | ABC 行为功能分布 |
| GET | `/student-evaluation/behavior-description-trend` | B 字段自由文本 TOP N 与月度趋势 |

## 周期规则

- `DAILY`：参考日期当天。
- `WEEKLY`：参考日期所在周周一至周日；`dailyTrends` 固定返回 7 项，无记录日期为 0。
- `MONTHLY`：参考日期所在自然月；`weeklyBreakdown` 按周一至周日切分为 4–6 段，尚未到来的周不返回。
- 行为环比的上一周期分别为前一天、上一自然周、上一自然月。

## 三态与百分比

- `INCOMPLETE`：未完成；`ASSISTED`：辅助完成；`INDEPENDENT`：独立完成。
- 快速记录的 `statusCode` 初始为 `null`，通过 `unclassifiedCount` 返回。
- 独立完成率只以三态已分类记录为分母，未分类记录不进入分母。
- `items`、`courseStats`、`environmentStats` 均直接由后端聚合，前端不再逐条请求或估算。

## 训练目标覆盖

`overview.trainingGoalCount` 统计周期内实际发生记录关联的全部标准目标去重数，不限于当前学生是否激活。每条行为记录计入其主行为关联的全部目标。

## ABC 与行为表现

- ABC 功能固定返回 `ATTENTION`、`ESCAPE`、`SENSORY`、`TANGIBLE`、`OTHER` 5 项，无记录项为 0。
- ABC 分母仅包含已保存详细记录且已选择行为功能的记录。
- 行为表现趋势仅统计 `detailSaved=true` 且 B 字段非空的记录，按 trim 后完全一致文本分组。
- 月度趋势将第 1 个自然周与最后一个实际有数据的自然周比较，返回 `UP`、`DOWN`、`STABLE`。

AI 智能分析、学期报告、导出和分享仍等待后续需求。
