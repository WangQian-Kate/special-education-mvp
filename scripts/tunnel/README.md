# HTTPS 内网穿透脚本

本目录使用 cpolar 将本机 `http://127.0.0.1:3000` 暴露为临时 HTTPS 地址，供微信真机和远程前端联调。

## 首次配置

1. 登录 [cpolar 控制台](https://dashboard.cpolar.com/auth) 获取 `authtoken`。
2. 在 PowerShell 中执行：

```powershell
.\scripts\tunnel\configure-cpolar.cmd
```

脚本会安全提示输入令牌，并调用 cpolar 写入 `%USERPROFILE%\.cpolar\cpolar.yml`。令牌不会写入项目仓库。

## 启动隧道

确保后端已经运行，然后执行：

```powershell
.\scripts\tunnel\start-cpolar-tunnel.cmd
```

脚本会依次完成：

- 验证本地 `GET /api/health`；
- 后台启动 cpolar；
- 自动读取 cpolar 的 HTTPS 地址；
- 从公网再次验证统一响应包体；
- 输出可直接填写到前端 `BASE_URLS.tunnel` 的 `https://.../api` 地址。

运行信息保存在 `work/tunnel/`，该目录已被 Git 忽略。

## 再次验证

```powershell
.\scripts\tunnel\test-cpolar-tunnel.cmd
```

也可以验证指定地址：

```powershell
.\scripts\tunnel\test-cpolar-tunnel.cmd -BaseUrl 'https://示例域名.cpolar.cn/api'
```

## 停止隧道

```powershell
.\scripts\tunnel\stop-cpolar-tunnel.cmd
```

停止脚本只处理由本项目启动并记录 PID 的 cpolar 进程，不会停止其他进程。

`.cmd` 入口只为当前脚本进程启用 PowerShell 执行权限，不会修改系统或当前用户的永久执行策略。
