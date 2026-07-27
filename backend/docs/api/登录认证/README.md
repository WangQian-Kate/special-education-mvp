# 登录认证接口

## 接口目录

| 方法 | 路径 | 是否需要 Bearer | 说明 |
|---|---|---|---|
| POST | `/auth/wechat/login` | 否 | 微信登录；首次登录同时绑定教师 |
| POST | `/auth/logout` | 是 | 注销当前设备会话 |

## 微信登录

前端先调用 `wx.login()`，然后提交：

```json
{
  "code": "微信临时登录凭证",
  "bindingCode": "ABCD-EFGH"
}
```

`bindingCode` 仅在当前 OpenID 尚未关联教师时提交。绑定码通过项目工具生成：

```powershell
.\scripts\auth\create-teacher-binding-code.cmd -TeacherId t001
```

成功响应中的 `accessToken` 后续作为请求头：

```http
Authorization: Bearer <accessToken>
```

登录响应同时返回 `profile`，包含当前教师、当前学生和是否需要选择学生。会话默认有效 7 天，同一教师允许多设备登录。

## 退出登录

调用：

```http
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

成功后前端删除本地 token。当前 token 立即失效，其他设备的会话不受影响。

## 错误码

| code | HTTP | 含义 |
|---:|---:|---|
| `40010` | 400 | 微信 code 无效或已经使用 |
| `40011` | 400 | 教师绑定码无效、已使用或已过期 |
| `40102` | 401 | Bearer 会话缺失、无效、已注销或已过期 |
| `40301` | 403 | 当前微信尚未关联教师，需要绑定码 |
| `40903` | 409 | 当前微信或教师已存在其他关联 |
| `50201` | 502 | 微信 code2Session 服务暂时不可用 |
| `50301` | 503 | 后端未配置 AppID 或 AppSecret |

## 安全约定

- AppSecret 只通过后端环境变量 `WECHAT_APP_SECRET` 提供。
- 前端不得调用 `api.weixin.qq.com`，也不得接触 AppSecret。
- 后端不保存微信 `session_key`。
- OpenID、微信临时 code、AppSecret 和原始 token 不写入日志。
- 数据库只保存 accessToken 和一次性绑定码的 SHA-256 哈希。
