# 特殊教育课堂助手后端

本项目是特殊教育课堂助手的 Spring Boot 后端。当前成员 B 阶段已经完成正式数据库、观察周期、行为记录、行为查询与行为统计，并提供可直接导入 Apifox 的 OpenAPI 3.0 文档。

当前实现范围对应前端“随班记录”页面；训练计划、学生评估和“我的”页面目前只完成数据库结构，尚未实现接口。认证、微信登录、AI 分析、PDF 导出和离线同步不在本阶段范围内。

## 1. 技术栈与环境

| 项目 | 版本或说明 |
|---|---|
| JDK | 25 |
| Spring Boot | 4.1.0 |
| Maven | 3.9.16，由 Maven Wrapper 提供 |
| MyBatis | 4.0.0 |
| MySQL | 8.0 |
| 默认服务地址 | `http://127.0.0.1:8080` |
| 默认数据库 | `special_ed_assistant` |

Windows 环境不需要单独安装 Maven，后续命令统一使用仓库中的 `mvnw.cmd`。

## 2. 目录说明

```text
backend/
├─ docs/
│  ├─ openapi.yaml       # OpenAPI 0.3.0，可导入 Apifox
│  └─ 接口文档.md         # 中文接口与请求示例
├─ sql/
│  ├─ schema.sql         # 数据库及 15 张正式表
│  └─ seed.sql           # 演示师生、字典和课程行为映射
├─ src/main/java/        # Controller、Service、Mapper、Entity、DTO
├─ src/main/resources/   # Spring 配置和 MyBatis XML
├─ src/test/java/        # Controller、Service 和 MySQL 集成测试
├─ mvnw.cmd              # Windows Maven Wrapper
└─ pom.xml
```

后端依赖方向固定为：

```text
Controller → Service → Mapper XML → MySQL
```

## 3. 数据库初始化

### 3.1 创建表和初始化字典

首次运行前，使用具备建库权限的 MySQL 管理员账号，依次执行：

1. `sql/schema.sql`
2. `sql/seed.sql`

`schema.sql` 创建 `special_ed_assistant` 数据库及 15 张表；`seed.sql` 写入一组演示师生数据、基础字典和课程行为映射。两个脚本均可重复执行，不包含 `DROP`、`TRUNCATE` 或业务数据清空操作。

### 3.2 应用数据库账号

应用默认使用 `special_ed_app@localhost`。该账号只需要以下权限：

- `SELECT`
- `INSERT`
- `UPDATE`
- `DELETE`

不要授予全局管理权限，也不要把真实密码写入 SQL、配置文件、README 或 Git。若本机尚未创建应用账号，应由数据库管理员创建账号，并只对 `special_ed_assistant.*` 授予上述四项权限。

### 3.3 演示数据

种子脚本提供：

- 演示教师：`id = 1`
- 演示学生：`id = 1`
- 教师与学生绑定关系：`1 → 1`

这组数据可用于本地联调。脚本不会创建虚假的观察周期、行为记录、训练计划或教师评价。

## 4. 应用配置

数据库密码必须通过环境变量传入。在 PowerShell 中执行：

```powershell
$env:DB_PASSWORD='<special_ed_app 的本地密码>'
```

可选配置如下：

| 环境变量 | 默认值 | 用途 |
|---|---|---|
| `DB_PASSWORD` | 无，必填 | 应用数据库密码 |
| `DB_URL` | `jdbc:mysql://localhost:3306/special_ed_assistant?...` | JDBC 地址 |
| `DB_USERNAME` | `special_ed_app` | 数据库账号 |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring 配置环境 |
| `SERVER_PORT` | `8080` | HTTP 端口 |

默认服务只监听 `127.0.0.1`。如需让局域网或其他设备访问，应先与项目成员确认部署和安全方案，不要直接修改为公网监听。

## 5. 测试、启动与打包

以下命令均在 `backend` 目录执行。

### 5.1 运行全部测试

```powershell
$env:DB_PASSWORD='<special_ed_app 的本地密码>'
.\mvnw.cmd test
```

设置 `DB_PASSWORD` 后，测试会包含真实 MySQL Mapper 集成测试，并在事务中自动回滚，不留下测试数据。未设置该变量时，数据库集成测试会跳过，其余 Controller 和 Service 测试仍会执行。

当前测试基线为 23 项：

- Controller 测试 12 项
- Service 测试 10 项
- MySQL Mapper 集成测试 1 项

### 5.2 启动开发服务

```powershell
$env:DB_PASSWORD='<special_ed_app 的本地密码>'
.\mvnw.cmd spring-boot:run
```

服务启动后进行健康检查：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/health
```

预期结果：

```json
{
  "status": "UP"
}
```

### 5.3 构建可运行包

```powershell
.\mvnw.cmd clean package
```

构建产物位于 `target/`。启动打包后的服务时仍需提供 `DB_PASSWORD`。

## 6. 已实现接口

| 模块 | 方法 | 路径 | 用途 |
|---|---|---|---|
| 系统 | GET | `/health` | 服务健康检查 |
| 观察周期 | POST | `/observation-session` | 创建指定学生的观察周期 |
| 观察周期 | GET | `/observation-session/{sessionId}` | 查询观察周期 |
| 观察周期 | PATCH | `/observation-session/{sessionId}` | 更新观察信息和本周期行为备注 |
| 行为记录 | POST | `/behavior` | 创建快速或详细行为记录 |
| 行为记录 | GET | `/behavior/{studentId}` | 按条件查询学生行为记录 |
| 行为记录 | PATCH | `/behavior/records/{recordId}` | 补充或编辑 ABC、辅助方式和详情 |
| 行为记录 | DELETE | `/behavior/records/{recordId}` | 删除行为记录 |
| 行为统计 | GET | `/statistics/{studentId}` | 查询频次、占比、趋势和环境分布 |

完整字段、状态码和示例请查看：

- `docs/openapi.yaml`：机器可读接口契约，以此文件为准。
- `docs/接口文档.md`：中文联调说明和请求示例。
- `../docs/design/member-b-final-verification.md`：本阶段最终验收记录。

## 7. 数据与接口约定

- 创建观察周期必须提供 `studentId` 和 `creatorId`。
- 当前尚未接入登录，`creatorId` 暂由前端传入；接入认证后应改由服务端身份确定。
- 快速行为记录只要求 B 行为，频次默认值为 1。
- 课程、环境、A/B/C 和辅助方式发送稳定的大写下划线 code；响应同时返回中文 label。
- 课程、环境或辅助方式选择 `OTHER` 时，必须同时提供对应说明。
- 行为发生时间转换为上海时区保存，并以 `+08:00` 返回。
- 查询日期范围包含开始日和结束日，`startDate` 与 `endDate` 必须同时传入。
- 统计按 `frequency` 聚合，占比四舍五入保留两位小数。
- 前端必须保存新建行为返回的记录 ID，后续编辑和删除都使用该 ID。
- 错误响应统一包含 `code` 和 `message`，当前稳定错误码为 `VALIDATION_ERROR`、`RESOURCE_NOT_FOUND` 和 `INTERNAL_ERROR`。

## 8. Apifox 使用

`docs/openapi.yaml` 当前版本为 0.3.0，包含 9 个接口和 14 个数据模型。该版本已经导入 Apifox 项目“特殊教育课堂助手 API”。

后续契约变更时，应把新文件导入现有模块，并采用以下合并方式：

- 相同 Method 与 Path 的接口覆盖更新。
- 接口继续保留原目录。
- 新数据源中不存在的现有资源不删除。

这样可以更新契约，同时避免创建重复模块或误删其他成员维护的内容。

## 9. 当前边界

本阶段暂不包含：

- 训练计划接口
- 学生评估接口
- “我的”页面接口
- 登录、鉴权和微信登录
- AI 分析与报告接口
- PDF 导出
- 离线同步
- 历史记录分页
- 多人同时编辑冲突处理

课程行为配置是否按学生维护、训练计划等级全集、A/B/C 字典全集以及 AI/PDF/离线数据是否持久化，需要在后续阶段与前端或成员 C 单独确认。

## 10. 当前验收状态

截至 2026-07-16，本阶段已完成：

- 正式 MySQL 数据库、15 张表和最小权限应用账号验证。
- 观察周期、快速记录、详细补充、筛选、统计、编辑和删除接口。
- 23 项自动化测试及真实 MySQL 事务回滚验证。
- Spring Boot 启动和真实 HTTP 主流程验收。
- OpenAPI 0.3.0 更新并导入 Apifox 现有项目。
