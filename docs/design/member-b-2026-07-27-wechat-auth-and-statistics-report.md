# 成员 B 正式微信登录与统计优化验收报告

> 日期：2026-07-27
> 任务来源：`7.27-7.28.md` 成员 B
> 范围：正式微信登录、接口异常优化、学生评估统计查询优化

## 一、完成内容

### 1. 正式微信登录

- 新增 `POST /api/auth/wechat/login`，由后端调用微信 `code2Session` 获取 OpenID。
- 已绑定 OpenID 直接登录；未绑定 OpenID 必须提交一次性教师绑定码。
- 新增 `POST /api/auth/logout`，只注销当前设备会话。
- 登录成功返回随机 Bearer token、到期时间、首次绑定标记和个人资料。
- 业务接口优先从 `Authorization: Bearer <accessToken>` 解析教师。
- 生产环境禁用 `X-Teacher-Id`；dev/test 环境保留兼容开关。

### 2. 身份与会话安全

- `wechat_identity` 保存小程序、OpenID 和既有教师账号的唯一关联。
- `teacher_binding_code` 只保存一次性绑定码 SHA-256 哈希、过期时间和使用时间。
- `auth_session` 只保存 accessToken SHA-256 哈希、过期时间和注销时间。
- 微信 `session_key` 不落库。
- AppSecret、OpenID、微信临时 code 和原始 token 不写入应用日志。
- 同一教师允许多设备登录，退出当前设备不影响其他会话。

### 3. 异常与响应契约

所有接口继续使用 `{ code, message, data }` 统一包体。新增错误码：

| code | HTTP | 说明 |
|---:|---:|---|
| `40010` | 400 | 微信 code 无效或已经使用 |
| `40011` | 400 | 教师绑定码无效、已使用或已过期 |
| `40102` | 401 | Bearer 会话无效、已注销或已过期 |
| `40301` | 403 | 当前微信尚未关联教师 |
| `40903` | 409 | 微信或教师身份绑定冲突 |
| `50201` | 502 | 微信 code2Session 服务暂时不可用 |
| `50301` | 503 | 后端未配置微信凭据 |

### 4. 数据库迁移

- `migrate-v3.0-wechat-auth.sql`：新增微信身份、教师绑定码和登录会话三张表。
- `migrate-v3.1-reporting-indexes.sql`：新增学生评估统计覆盖索引。
- 本机正式数据库已经应用 `3.0.0` 和 `3.1.0`。
- 当前数据库为 35 张表、164 个字段、88 个索引、44 个外键。

### 5. 学生评估统计性能

新增索引：

- `idx_class_record_student_date_dimensions(student_id, record_date, course_code, environment_code)`
- `idx_behavior_record_reporting(class_record_id, detail_saved, function_code, status_code)`

使用隔离临时库生成 1,000 条课堂记录和 100,000 条行为记录，测试月度查询；测试后临时库已删除。

| 查询 | 五次平均 | 最大值 |
|---|---:|---:|
| `/student-evaluation/statistics` 对应 6 条 SQL | 186.51ms | 207.19ms |
| `/student-evaluation/abc-distribution` | 52.71ms | 62.13ms |
| `/student-evaluation/behavior-description-trend` | 64.63ms | 73.85ms |

查询未发现 N+1；学生和日期使用复合索引，ABC 分布使用详细记录/功能覆盖索引。

## 二、自动化验收

新增或修复以下场景：

- 首次微信登录和教师绑定；
- 已绑定微信再次登录；
- 未绑定微信返回绑定要求；
- Bearer token 访问业务接口；
- 生产规则下拒绝 `X-Teacher-Id`；
- 退出后 token 立即失效；
- 原始 token 不落库；
- 微信无效 code 转换为稳定错误码；
- 微信上游 HTTP 故障转换为网关错误；
- OpenAPI YAML 解析及正式认证契约校验；
- 2026 年 7 月自然周正确拆分为 5 段；
- 修复最新 AI 提交中响应模型和数据实体不同步造成的编译错误，不改变 AI Prompt。

## 三、前端必须同步的改动

1. 启动时调用 `wx.login()`，把 code 提交给 `POST /auth/wechat/login`。
2. 首次未绑定时展示绑定码输入框，收到 `40301` 后由教师填写一次性绑定码重试。
3. 保存登录响应中的 `accessToken` 和 `expiresAt`。
4. 所有业务请求改为 `Authorization: Bearer <accessToken>`。
5. 收到 HTTP 401 / `40102` 时清除 token 并返回欢迎页。
6. 退出登录调用 `POST /auth/logout`，成功后清除本地 token。
7. AppSecret 只能在后端配置，前端不得保存或提交。
8. 微信公众平台的 request 合法域名填写 HTTPS 域名，不包含 `/api`；前端 BASE_URL 继续包含 `/api`。

## 四、验收结论

成员 B 的正式微信登录、接口认证异常和学生评估统计性能优化已经完成后端实现。接口契约版本更新为 `0.14.0`，数据库版本更新为 `3.1.0`。最终真机登录仍需前端提供实时 `wx.login()` code，并使用已配置合法域名的 HTTPS 地址联合验证。
