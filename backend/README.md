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

## 当前成员 B 接口骨架

- `POST /behavior`：创建结构化 ABC 行为记录。
- `GET /behavior/{studentId}`：查询学生行为记录。
- `GET /statistics/{studentId}`：按行为类型统计学生记录。

可将 `docs/openapi.yaml` 手动导入 Apifox。种子脚本创建 ID 为 `1` 的演示教师、演示学生及其绑定关系。

当前接口和 Mapper 是正式数据库创建前的早期骨架，其中行为记录 SQL 仍使用旧字段。健康接口和数据库连接已经验证；行为接口将在下一项“接口框架与持久层适配”任务中统一切换到正式表结构，切换前不要将早期行为接口作为可联调版本。
