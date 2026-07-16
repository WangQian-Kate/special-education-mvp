# 特殊教育课堂行为记录后端

## 环境要求

- JDK 25
- Maven 3.9.16
- MySQL 8.0

## 数据库初始化

首次运行前，使用具备建库权限的 MySQL 管理员账号依次执行：

1. `sql/schema.sql`
2. `sql/seed.sql`

建库脚本创建 `special_ed_assistant` 数据库及 15 张业务表。种子脚本写入一组演示师生数据、已确认的基础字典和课程行为映射；两个脚本均可重复执行。

应用使用本机账号 `special_ed_app` 连接数据库。该账号只需要 `special_ed_assistant.*` 上的 `SELECT`、`INSERT`、`UPDATE`、`DELETE` 权限，不应授予建表或全局管理权限。

## 配置方式

启动前在当前终端设置数据库密码，密码不得写入 Git：

```powershell
$env:DB_PASSWORD='<本地开发密码>'
```

如需覆盖默认配置，可设置：

- `DB_URL`：数据库连接地址。
- `DB_USERNAME`：数据库用户名，默认 `special_ed_app`。
- `SPRING_PROFILES_ACTIVE`：Spring 配置环境，默认 `dev`。
- `SERVER_PORT`：服务端口，默认 `8080`。

## 验证与启动

在 `backend` 目录执行：

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

健康检查接口为 `GET http://localhost:8080/health`。

## 当前成员 B 接口

- `GET /health`：服务健康检查。
- `POST /observation-session`：创建观察周期。
- `GET /observation-session/{sessionId}`：查询观察周期。
- `PATCH /observation-session/{sessionId}`：实时更新观察信息和本周期行为备注。
- `POST /behavior`：创建快速或详细行为记录。
- `GET /behavior/{studentId}`：按日期、课程、环境或观察周期查询行为记录。
- `PATCH /behavior/records/{recordId}`：补充或编辑行为详细记录。
- `DELETE /behavior/records/{recordId}`：删除行为记录。
- `GET /statistics/{studentId}`：查询行为频次、占比、日期趋势和环境分布。

接口使用稳定字典 code，响应同时返回中文 label。行为发生时间按上海时区保存并以 `+08:00` 返回，统计占比按 `frequency` 计算并保留两位小数。

## 测试说明

Controller 和 Service 测试不依赖数据库。Mapper 集成测试会在存在 `DB_PASSWORD` 环境变量时连接本机正式数据库，并在事务中执行后自动回滚；没有该变量时只跳过数据库集成测试。

种子脚本创建 ID 为 `1` 的演示教师、演示学生及其绑定关系，供本地联调使用。完整接口契约位于 `docs/openapi.yaml`，更新后需要重新手动导入 Apifox。
