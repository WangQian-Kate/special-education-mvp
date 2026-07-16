# 特殊教育课堂助手后端

## 当前状态

项目正在进行 V2 重构，设计与运行状态必须分开理解：

| 内容 | 当前状态 |
|---|---|
| V2 数据模型 | 已形成设计稿，等待审核 |
| OpenAPI | 已重写为 0.4.0 设计稿 |
| 页面接口说明 | 已按随班记录、训练计划、学生评估、“我的”、系统分目录 |
| Java 代码 | 仍是旧 V0.3 实现 |
| 正式 MySQL | 仍是旧 V0.3 表结构 |
| Apifox | 仍是旧 V0.3 接口 |

因此，`docs/openapi.yaml` 中的新接口目前不能直接调用，也不要在本批审核完成前导入 Apifox。

## V2 设计入口

- `docs/openapi.yaml`：OpenAPI 0.4.0 统一契约。
- `docs/接口文档.md`：V2 中文接口总览。
- `docs/api/随班记录/README.md`
- `docs/api/训练计划/README.md`
- `docs/api/学生评估/README.md`
- `docs/api/我的/README.md`
- `docs/api/系统/README.md`
- `../docs/design/member-b-v2-data-model.md`：V2 数据模型。
- `../docs/design/member-b-v2-api-design.md`：V2 API 总设计。

## 技术栈

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

## 目录结构

```text
backend/
├─ docs/
│  ├─ api/               # 按前端页面划分的 V2 接口说明
│  ├─ openapi.yaml       # OpenAPI 0.4.0 设计稿
│  └─ 接口文档.md         # V2 中文总览
├─ sql/                  # 当前仍是旧 V0.3 SQL，下一批重构
├─ src/main/             # 当前仍是旧 V0.3 Java 代码
├─ src/test/             # 当前仍是旧 V0.3 测试
├─ mvnw.cmd
└─ pom.xml
```

后续代码也将按页面建立模块目录：

```text
api/
├─ classrecord/        # 随班记录
├─ trainingplan/       # 训练计划
├─ studentevaluation/  # 学生评估
├─ profile/            # 我的
└─ system/             # 系统
```

## 当前运行配置

数据库密码只通过环境变量提供，不能写入 Git：

```powershell
$env:DB_PASSWORD='<special_ed_app 的本地密码>'
```

| 环境变量 | 默认值 | 用途 |
|---|---|---|
| `DB_PASSWORD` | 无，必填 | 应用数据库密码 |
| `DB_URL` | `jdbc:mysql://localhost:3306/special_ed_assistant?...` | JDBC 地址 |
| `DB_USERNAME` | `special_ed_app` | 数据库账号 |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring 配置环境 |
| `SERVER_PORT` | `8080` | HTTP 端口 |

应用账号只需要 `special_ed_assistant.*` 上的 `SELECT`、`INSERT`、`UPDATE`、`DELETE` 权限。

## 运行旧 V0.3 基线

以下命令仍可用于确认重构前基线：

```powershell
$env:DB_PASSWORD='<special_ed_app 的本地密码>'
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

健康检查：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/health
```

旧基线共有 23 项测试，V2 Java 实施后将重写相应测试，不能继续把旧测试通过视为 V2 验收通过。

## V2 已确认规则

- 除健康检查外，业务接口暂时使用 `X-User-Id` 表示当前用户。
- 后端保存当前学生，并校验用户与学生绑定。
- 一条行为记录代表一次发生，次数直接统计记录条数。
- 快速记录时间由后端生成，补记由前端提交过去时间。
- A、B、C 为自由文本。
- 辅助方式保存方式 code 和每项必填内容。
- 详细记录首次保存后永久标记为已保存。
- 日记录可编辑，周记录和月记录只读。
- 学生评估统计来自随班记录。
- 训练计划以 163 项标准目标库为主，同时允许当前学生创建自定义目标。

## 暂缓内容

- 163 项标准目标具体数据
- 训练等级全集
- 行为环节选项
- 行为功能选项
- 学期固定日期
- AI 智能分析
- 导出与分享
- 学生信息管理
- 个人资料修改
- 登录和退出登录
