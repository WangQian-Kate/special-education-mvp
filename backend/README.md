# 特殊教育课堂助手后端

## 当前状态

成员 B 的 V2 后端已按最终确认的软件原型完成重构，并已切换到正式本地数据库。

| 内容 | 当前状态 |
|---|---|
| OpenAPI 契约 | `0.12.0`，共 28 个接口 |
| Java 实现 | 已完成，按前端页面分包 |
| MySQL | `special_ed_assistant` 已迁移到 `2.7.0`，内置 100 个标准行为、三态完成字典及完整映射 |
| 自动化测试 | 8 个数据库集成场景覆盖核心业务及全部新增接口，已通过 |
| Apifox | 需将 0.12.0 重新导入现有新版模块 |

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
| 训练计划 | 9 |
| 学生评估 | 3 |
| 我的 | 3 |
| 系统 | 1 |
| 合计 | 28 |

详细契约见：

- `docs/openapi.yaml`：OpenAPI 0.12.0 统一契约。
- `docs/behavior-catalog.json`：由确认版 Markdown 自动生成的行为目录交换文件。
- `docs/接口文档.md`：中文接口总览。
- `docs/api/`：按页面拆分的中文接口说明。
- `../docs/design/member-b-v2-data-model.md`：V2 数据模型。
- `../docs/design/member-b-v2-api-design.md`：V2 API 总设计。
- `../docs/design/member-b-v2-database-migration.md`：迁移方案和正式迁移记录。
- `../docs/design/member-b-v2-implementation-verification.md`：本次实现与验收报告。
- `../docs/design/member-b-2026-07-22-behavior-catalog-report.md`：行为目录与状态栏实施报告。

## 技术环境

| 项目 | 版本或说明 |
|---|---|
| JDK | 25 |
| Spring Boot | 4.1.0 |
| Maven | 3.9.16，由 Maven Wrapper 提供 |
| MyBatis | 4.0.0 |
| MySQL | 8.0 |
| 默认服务地址 | `http://localhost:3000/api` |
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
| `SERVER_ADDRESS` | `127.0.0.1` | HTTP 监听地址；内网穿透时可改为 `0.0.0.0` |
| `SERVER_PORT` | `3000` | HTTP 端口 |

运行测试和服务：

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

健康检查：

```powershell
Invoke-RestMethod http://localhost:3000/api/health
```

所有接口统一返回以下包体：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

`code` 为 `0` 表示成功；非 `0` 时前端展示 `message`。删除成功同样返回 HTTP 200 和统一包体，其中 `data` 为 `null`。

除健康检查外，业务接口只接受请求头 `X-Teacher-Id: t001/t002/t003`。后端通过白名单映射读取教师及其绑定学生，不再接受 `X-User-Id`。缺少或无法识别教师身份时返回 HTTP 401、错误码 `40101`。

## HTTPS 内网穿透

本机已准备 cpolar 3.3.12 便携客户端：

```text
%LOCALAPPDATA%\Programs\cpolar-portable\cpolar.exe
```

项目已提供完整的配置、启动、验证和停止脚本，说明见 [`../scripts/tunnel/README.md`](../scripts/tunnel/README.md)。首次使用时执行：

```powershell
.\scripts\tunnel\configure-cpolar.cmd
.\scripts\tunnel\start-cpolar-tunnel.cmd
```

启动脚本会验证本地和公网的 `GET /api/health`，并输出可直接填写到前端 `BASE_URLS.tunnel` 的 `https://.../api` 地址。cpolar 在本机连接 `127.0.0.1:3000`，因此无需为了内网穿透将后端监听地址改为 `0.0.0.0`；只有局域网直连时才需要修改 `SERVER_ADDRESS`。

## 数据库脚本

| 文件 | 用途 |
|---|---|
| `sql/schema.sql` | 全新安装 V2 数据库结构 |
| `sql/seed.sql` | 本地开发基础字典和演示数据，可重复执行 |
| `sql/migrate-v2.sql` | V0.3 空业务库升级到 V2，检测到旧业务数据时自动中止 |
| `sql/migrate-v2.1-training-goals.sql` | V2.0.0 增加标准目标业务编号结构 |
| `sql/migrate-v2.2-whitelist-identities.sql` | V2.1.0 增加教师/学生外部编号、性别和白名单数据 |
| `sql/migrate-v2.3-assistance-validation.sql` | V2.2.0 替换辅助方式字典并允许辅助内容为空 |
| `sql/migrate-v2.4-behavior-functions.sql` | V2.3.0 增加 4 项行为功能字典 |
| `sql/migrate-v2.5-course-environment-dictionaries.sql` | V2.4.0 收敛为 16 项课程和 7 项环境字典 |
| `sql/migrate-v2.6-behavior-catalog.sql` | V2.5.0 增加标准行为目录、分组、状态和训练目标关联结构 |
| `sql/migrate-v2.7-reporting-status.sql` | V2.6.0 增加行为三态、ABC“其他”及统计索引 |

正式本地数据库已升级到 `2.7.0`、共 28 张表，并包含 3 名白名单教师、6 名绑定学生、16 项课程、7 项环境、5 项行为功能、3 项行为完成状态、100 个主行为、88 个子行为、531 个表现/状态选项、28 个分组、3 个分组状态栏选项和编号连续的 165 项标准训练目标。应用账号只需要 `special_ed_assistant.*` 上的 `SELECT`、`INSERT`、`UPDATE`、`DELETE` 权限。

`seed.sql` 只用于本地开发，不应直接用于生产数据环境。

## 已实现的关键规则

- 后端保存当前学生，并校验用户与学生绑定关系。
- 一条行为记录代表一次发生，次数按记录条数统计。
- 快速记录时间由后端生成；补记必须提交过去的发生时间。
- A、B、C 使用自由文本。
- 行为卡片由课程和环境共同筛选；`ALL_DAY_SUMMARY` 忽略课程限制，但仍按环境筛选。
- 行为目录使用稳定编码 `B001-B100`，并返回模块、分组、推荐课程/环境、训练目标、子行为和可多选状态。
- 详细记录可保存标准子行为和状态选项；状态必须属于当前主行为，“其它”必须填写自定义文本。
- 辅助方式限定为 9 项稳定 code，勾选后可不填写补充内容。
- 快速记录状态默认为空；详细保存显式状态优先，未显式选择但勾选辅助方式时自动归为 `ASSISTED`。
- 行为功能固定为 5 项；选择 `OTHER` 时必须提供 `functionOtherText`。
- 详细记录任意一项有值即可保存，全部为空时拒绝；首次保存成功后 `detailSaved` 永久保持为 `true`。
- 日记录可编辑；周记录和月记录只读。
- 学生评估直接统计随班记录数据，返回三态分布、7 天趋势、自然周拆分、课程/环境统计、ABC 功能分布及行为表现趋势。
- 训练计划支持标准目标分配和当前学生的自定义目标。
- 训练计划返回全部已激活目标的周期进度，并支持按标准目标查询最新关联行为记录。
- 标准目标使用独立的 `standardNumber` 返回业务编号 `1-165`；自定义目标该字段为 `null`。
- 训练目标状态与阶段修改后立即保存；删除必须显式确认。

## 待外部资料或后续页面确认

- 训练等级全集。
- 行为环节选项。
- 学期报告固定日期。
- AI 智能分析。
- 导出与分享。
- 学生信息管理。
- 个人资料修改。
- 登录、认证和退出登录。

这些内容尚未由前端或其他成员提供，当前接口会返回空的对应字典，不会虚构业务数据。标准目标库已经包含本次提供的全部 165 项。
