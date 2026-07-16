# 成员 B 后端开发环境与最小工程设计

## 目标

为成员 B 配置可复现的 Java 后端开发环境，并建立一个能通过自动化测试、能连接 MySQL、能由 Apifox 调用的最小 Spring Boot 工程，支撑后续数据库设计和行为记录 API 开发。

## 已确认范围

- 后端技术栈采用 Spring Boot，不采用 FastAPI。
- 数据库采用本机 MySQL 8.0。
- 数据访问层采用 MyBatis，对应任务文档中的 `mapper` 分层。
- 使用现有 IntelliJ IDEA、JDK 25、Git 和 MySQL Workbench。
- 安装 Maven 3.9.x 和 Apifox。
- 不重置 MySQL 密码；只验证现有凭据和连接。
- 本阶段不配置 Docker、云服务器、内网穿透、微信开发者工具和 AI API Key。

## 版本组合

- Java：现有 Oracle JDK 25.0.2。
- Spring Boot：4.1.0。
- MyBatis Spring Boot Starter：4.0.0。
- Maven：3.9.16，并在项目中保留 Maven Wrapper。
- MySQL：现有 MySQL Server 8.0.45。
- Git：现有 Git 2.55.0.windows.2。

该组合采用 Spring Boot 与 MyBatis 官方兼容矩阵支持的版本。若第三方依赖在构建时暴露与 Java 25 的具体兼容问题，再降级为并行安装 JDK 21；不预先替换当前 JDK。

## 环境配置设计

1. 安装 Maven 3.9.16，设置用户级 `MAVEN_HOME`，并把 Maven `bin` 加入用户 PATH。
2. 修复用户 PATH 中失效的 `C:\MySQL\MySQL_Server 8.0\bin`，替换为实际目录 `C:\Program Files\MySQL\MySQL Server 8.0\bin`。
3. 保留现有 `JAVA_HOME=C:\develop-java\JDK`，以 `java --version` 和 `mvn --version` 联合验证 Maven 使用正确 JDK。
4. 安装 Apifox，用于 API 契约、Mock 和接口调用。
5. 保持 `MySQL80` 为手动启动方式，只在开发时启动；不擅自改成开机自启。
6. 设置 Git 新仓库默认分支为 `main`，初始化项目仓库后创建 `develop` 分支。

## 最小工程结构

```text
backend/
├─ pom.xml
├─ mvnw
├─ mvnw.cmd
├─ .mvn/wrapper/
├─ .gitignore
├─ README.md
├─ sql/
│  ├─ schema.sql
│  └─ seed.sql
└─ src/
   ├─ main/
   │  ├─ java/.../
   │  │  ├─ controller/
   │  │  ├─ service/
   │  │  ├─ mapper/
   │  │  ├─ entity/
   │  │  ├─ dto/
   │  │  ├─ config/
   │  │  └─ exception/
   │  └─ resources/
   │     ├─ application.yml
   │     ├─ application-dev.yml
   │     └─ mapper/
   └─ test/
      └─ java/.../
```

## 依赖范围

- Spring Web：REST API。
- Spring Validation：请求参数校验。
- MyBatis Spring Boot Starter：Mapper 和 SQL 映射。
- MySQL Connector/J：MySQL JDBC 驱动。
- Spring Boot Test：JUnit、MockMvc 等测试支持。

暂不加入 Lombok、MyBatis-Plus、Spring Security、Redis、Flyway 或 AI SDK，避免在 Day 1–2 引入非必要复杂度。

## 配置与安全

- 数据库地址、用户名和密码从环境变量读取。
- Git 只提交变量占位示例，不提交真实数据库密码或 API Key。
- 默认服务仅监听本机；局域网监听和 Windows 防火墙规则留到前后端联调阶段单独确认。
- MySQL root 账户仅用于必要的本地初始化；应用使用独立数据库和最小权限用户。

## 数据设计约束

- Java/JSON 字段使用 lowerCamelCase，数据库字段使用 snake_case。
- A（前因）、B（行为）、C（后果）、场景和角色使用稳定代码或字典表外键，不保存任意展示文本作为统计键。
- 时间字段使用明确时区语义，API 采用 ISO 8601 格式。
- `schema.sql` 和 `seed.sql` 支持在新电脑上重建演示数据库。

## 最小功能与测试

第一项可运行功能为 `GET /health`：成功时返回 HTTP 200 和稳定 JSON。

实施采用测试先行：

1. 先编写 MockMvc 测试并确认因接口不存在而失败。
2. 再实现最小控制器使测试通过。
3. 运行完整 Maven 测试。
4. 启动应用并用 Apifox 调用 `/health`。
5. 验证 MySQL 服务、3306 端口和 JDBC 连接；若现有密码未知，只报告凭据阻塞，不重置密码。

## 验收标准

- `java --version`、`mvn --version`、`git --version` 和 `mysql --version` 均可从新终端执行。
- MySQL 服务能启动，3306 端口监听正常。
- `mvn test` 全部通过。
- 后端可启动且 `GET /health` 返回 HTTP 200。
- Apifox 已安装并可成功调用健康检查接口。
- Git 仓库具有 `main` 和 `develop` 分支，敏感配置未进入版本控制。

## 后续但不在本次配置范围

- 用户、学生、行为记录和字典表的完整建模。
- `POST /behavior`、`GET /behavior/{studentId}`、`GET /statistics/{studentId}`。
- 微信登录、AI 报告、外网访问、部署和防火墙开放。
