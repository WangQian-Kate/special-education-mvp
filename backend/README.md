# 特殊教育课堂助手后端

## 当前状态

成员 B 的 V2 后端已按最终确认的软件原型完成重构，并已切换到正式本地数据库。

| 内容 | 当前状态 |
|---|---|
| OpenAPI 契约 | `0.4.1`，共 24 个接口 |
| Java 实现 | 已完成，按前端页面分包 |
| MySQL | `special_ed_assistant` 已迁移到 `2.1.0`，内置 165 项标准目标 |
| 自动化测试 | 5 个数据库集成场景覆盖全部 24 个接口，已通过 |
| Apifox | 当前已导入 0.4.0；需将 0.4.1 重新导入现有新版模块 |

## 页面模块

```text
src/main/java/com/specialed/assistant/api/
├─ classrecord/        # 随班记录
├─ trainingplan/       # 训练计划
├─ studentevaluation/  # 学生评估
├─ profile/            # 我的
└─ system/             # 系统
```

接口数量如下：

| 页面模块 | 接口数 |
|---|---:|
| 随班记录 | 12 |
| 训练计划 | 7 |
| 学生评估 | 1 |
| 我的 | 3 |
| 系统 | 1 |
| 合计 | 24 |

详细契约见：

- `docs/openapi.yaml`：OpenAPI 0.4.1 统一契约。
- `docs/接口文档.md`：中文接口总览。
- `docs/api/`：按页面拆分的中文接口说明。
- `../docs/design/member-b-v2-data-model.md`：V2 数据模型。
- `../docs/design/member-b-v2-api-design.md`：V2 API 总设计。
- `../docs/design/member-b-v2-database-migration.md`：迁移方案和正式迁移记录。
- `../docs/design/member-b-v2-implementation-verification.md`：本次实现与验收报告。

## 技术环境

| 项目 | 版本或说明 |
|---|---|
| JDK | 25 |
| Spring Boot | 4.1.0 |
| Maven | 3.9.16，由 Maven Wrapper 提供 |
| MyBatis | 4.0.0 |
| MySQL | 8.0 |
| 默认服务地址 | `http://127.0.0.1:8080` |
| 默认数据库 | `special_ed_assistant` |

Windows 环境不需要单独安装 Maven，统一使用仓库中的 `mvnw.cmd`。

## 本地运行

数据库密码仅通过环境变量提供，禁止写入 Git：

```powershell
$env:DB_PASSWORD='<special_ed_app 的本地密码>'
```

可用环境变量如下：

| 环境变量 | 默认值 | 用途 |
|---|---|---|
| `DB_PASSWORD` | 无，必填 | 应用数据库密码 |
| `DB_URL` | `jdbc:mysql://localhost:3306/special_ed_assistant?...` | JDBC 地址 |
| `DB_USERNAME` | `special_ed_app` | 数据库账号 |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring 配置环境 |
| `SERVER_PORT` | `8080` | HTTP 端口 |

运行测试和服务：

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

健康检查：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/health
```

除健康检查外，当前业务接口使用请求头 `X-User-Id` 表示当前用户。未来接入认证后，由认证上下文替代。

## 数据库脚本

| 文件 | 用途 |
|---|---|
| `sql/schema.sql` | 全新安装 V2 数据库结构 |
| `sql/seed.sql` | 本地开发基础字典和演示数据，可重复执行 |
| `sql/migrate-v2.sql` | V0.3 空业务库升级到 V2，检测到旧业务数据时自动中止 |
| `sql/migrate-v2.1-training-goals.sql` | V2.0.0 增加标准目标业务编号结构 |

正式本地数据库已于 2026-07-17 完成迁移，当前版本为 `2.1.0`、共 18 张表，并包含编号连续的 165 项标准训练目标。应用账号只需要 `special_ed_assistant.*` 上的 `SELECT`、`INSERT`、`UPDATE`、`DELETE` 权限。

`seed.sql` 只用于本地开发，不应直接用于生产数据环境。

## 已实现的关键规则

- 后端保存当前学生，并校验用户与学生绑定关系。
- 一条行为记录代表一次发生，次数按记录条数统计。
- 快速记录时间由后端生成；补记必须提交过去的发生时间。
- A、B、C 使用自由文本。
- 辅助方式保存稳定 code，勾选项必须填写内容。
- 详细记录首次保存后，`detailSaved` 永久保持为 `true`。
- 日记录可编辑；周记录和月记录只读。
- 学生评估直接统计随班记录数据。
- 训练计划支持标准目标分配和当前学生的自定义目标。
- 标准目标使用独立的 `standardNumber` 返回业务编号 `1-165`；自定义目标该字段为 `null`。
- 训练目标状态与阶段修改后立即保存；删除必须显式确认。

## 待外部资料或后续页面确认

- 训练等级全集。
- 行为环节和行为功能选项。
- 学期报告固定日期。
- AI 智能分析。
- 导出与分享。
- 学生信息管理。
- 个人资料修改。
- 登录、认证和退出登录。

这些内容尚未由前端或其他成员提供，当前接口会返回空的对应字典，不会虚构业务数据。标准目标库已经包含本次提供的全部 165 项。
