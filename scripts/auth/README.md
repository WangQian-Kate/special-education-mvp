# 微信教师绑定码工具

正式微信首次登录时，后端需要用一次性绑定码把微信 OpenID 与既有教师账号关联。

在项目根目录执行：

```powershell
.\scripts\auth\create-teacher-binding-code.cmd -TeacherId t001
```

脚本会安全提示输入数据库密码，并生成有效期 30 分钟的 8 位随机绑定码。数据库只保存 SHA-256 哈希，不保存明文；绑定码使用一次后立即失效。

如需调整有效期：

```powershell
.\scripts\auth\create-teacher-binding-code.cmd -TeacherId t001 -ValidityMinutes 60
```

绑定码应单独发送给对应教师，不应写入 Git、接口文档或群聊。
