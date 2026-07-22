[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-CpolarExecutable {
    $command = Get-Command cpolar -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }

    $portable = Join-Path $env:LOCALAPPDATA 'Programs\cpolar-portable\cpolar.exe'
    if (Test-Path -LiteralPath $portable) {
        return $portable
    }

    throw '未找到 cpolar。请先安装 cpolar 客户端。'
}

$token = $env:CPOLAR_AUTHTOKEN
if ([string]::IsNullOrWhiteSpace($token)) {
    $secureToken = Read-Host '请输入 cpolar 控制台中的 authtoken' -AsSecureString
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureToken)
    try {
        $token = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}

if ([string]::IsNullOrWhiteSpace($token)) {
    throw 'authtoken 不能为空。'
}

$cpolar = Get-CpolarExecutable
& $cpolar authtoken $token
if ($LASTEXITCODE -ne 0) {
    throw "cpolar authtoken 配置失败，退出码：$LASTEXITCODE"
}

$configPath = Join-Path $HOME '.cpolar\cpolar.yml'
if (-not (Test-Path -LiteralPath $configPath)) {
    throw "cpolar 未生成配置文件：$configPath"
}

$configText = Get-Content -LiteralPath $configPath -Raw
if ($configText -notmatch '(?m)^\s*authtoken\s*:\s*\S+') {
    throw 'cpolar 配置文件中未检测到 authtoken。'
}

Write-Host "cpolar 认证已配置：$configPath" -ForegroundColor Green
Write-Host '令牌保存在用户目录中，不会写入项目仓库。'
