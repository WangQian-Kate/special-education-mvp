[CmdletBinding()]
param(
    [ValidateRange(1, 65535)]
    [int]$Port = 3000,

    [ValidateRange(5, 120)]
    [int]$WaitSeconds = 30
)

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

function Get-CpolarHttpsUrl {
    param(
        [int]$LocalPort,
        [string]$LogPath
    )

    try {
        $response = Invoke-RestMethod -Uri 'http://127.0.0.1:4040/api/tunnels' -TimeoutSec 2
    }
    catch {
        $response = $null
    }

    if ($null -ne $response -and $response.PSObject.Properties.Name -contains 'tunnels') {
        foreach ($tunnel in @($response.tunnels)) {
            $address = [string]$tunnel.config.addr
            if ($tunnel.public_url -like 'https://*' -and $address -match "(^|:)$LocalPort$") {
                return ([string]$tunnel.public_url).TrimEnd('/')
            }
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($LogPath) -and (Test-Path -LiteralPath $LogPath)) {
        $logText = (Get-Content -LiteralPath $LogPath -Tail 100) -join [Environment]::NewLine
        $matches = [regex]::Matches($logText, 'Tunnel established at (https://[^\s"]+)')
        if ($matches.Count -gt 0) {
            return $matches[$matches.Count - 1].Groups[1].Value.TrimEnd('/')
        }
    }

    return $null
}

function Test-HealthResponse {
    param([string]$Uri)

    $response = Invoke-RestMethod -Uri $Uri -Method Get -TimeoutSec 10
    return $response.code -eq 0 -and $response.data.status -eq 'UP'
}

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$stateDirectory = Join-Path $repoRoot 'work\tunnel'
$pidPath = Join-Path $stateDirectory 'cpolar.pid'
$urlPath = Join-Path $stateDirectory 'cpolar-base-url.txt'
$metadataPath = Join-Path $stateDirectory 'cpolar-tunnel.json'
$stdoutPath = Join-Path $stateDirectory 'cpolar.stdout.log'
$stderrPath = Join-Path $stateDirectory 'cpolar.stderr.log'

New-Item -ItemType Directory -Path $stateDirectory -Force | Out-Null

$localHealthUrl = "http://127.0.0.1:$Port/api/health"
if (-not (Test-HealthResponse -Uri $localHealthUrl)) {
    throw "本地后端健康检查失败：$localHealthUrl"
}

$configPath = Join-Path $HOME '.cpolar\cpolar.yml'
if (-not (Test-Path -LiteralPath $configPath)) {
    throw '尚未配置 cpolar。请先运行 scripts\tunnel\configure-cpolar.cmd。'
}

$configText = Get-Content -LiteralPath $configPath -Raw
if ($configText -notmatch '(?m)^\s*authtoken\s*:\s*\S+') {
    throw '尚未配置 cpolar authtoken。请先运行 scripts\tunnel\configure-cpolar.cmd。'
}

$cpolar = Get-CpolarExecutable
$managedProcess = $null
if (Test-Path -LiteralPath $pidPath) {
    try {
        $savedProcessId = [int](Get-Content -LiteralPath $pidPath -Raw)
        $candidateProcess = Get-Process -Id $savedProcessId -ErrorAction SilentlyContinue
        if ($null -ne $candidateProcess -and $candidateProcess.ProcessName -eq 'cpolar') {
            $managedProcess = $candidateProcess
        }
    }
    catch {
        $managedProcess = $null
    }
}

$existingLogPath = if ($null -ne $managedProcess) { $stdoutPath } else { $null }
$publicOrigin = Get-CpolarHttpsUrl -LocalPort $Port -LogPath $existingLogPath
$startedProcess = $managedProcess

if ($null -eq $publicOrigin -and $null -ne $managedProcess) {
    $existingDeadline = (Get-Date).AddSeconds($WaitSeconds)
    do {
        Start-Sleep -Milliseconds 500
        $publicOrigin = Get-CpolarHttpsUrl -LocalPort $Port -LogPath $stdoutPath
    } while ($null -eq $publicOrigin -and (Get-Date) -lt $existingDeadline -and -not $managedProcess.HasExited)
}

if ($null -eq $publicOrigin) {
    $webPort = Get-NetTCPConnection -LocalPort 4040 -State Listen -ErrorAction SilentlyContinue
    if ($null -ne $webPort) {
        $owner = Get-Process -Id $webPort.OwningProcess -ErrorAction SilentlyContinue
        throw "本机 4040 端口已被 $($owner.ProcessName) 占用，无法启动 cpolar 检查接口。"
    }

    $startedProcess = Start-Process -FilePath $cpolar `
        -ArgumentList @('http', '-log=stdout', '-log-level=INFO', $Port.ToString()) `
        -WindowStyle Hidden `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath `
        -PassThru
    Set-Content -LiteralPath $pidPath -Value $startedProcess.Id -Encoding ascii

    $deadline = (Get-Date).AddSeconds($WaitSeconds)
    do {
        Start-Sleep -Milliseconds 500
        $publicOrigin = Get-CpolarHttpsUrl -LocalPort $Port -LogPath $stdoutPath
    } while ($null -eq $publicOrigin -and (Get-Date) -lt $deadline -and -not $startedProcess.HasExited)
}

if ($null -eq $publicOrigin) {
    if ($null -ne $startedProcess -and -not $startedProcess.HasExited) {
        Stop-Process -Id $startedProcess.Id -Force
    }
    $errorTail = if (Test-Path -LiteralPath $stderrPath) {
        (Get-Content -LiteralPath $stderrPath -Tail 20) -join [Environment]::NewLine
    }
    else {
        '没有生成错误日志。'
    }
    throw "未能在 $WaitSeconds 秒内取得 HTTPS 地址。cpolar 日志：$([Environment]::NewLine)$errorTail"
}

$publicBaseUrl = "$publicOrigin/api"
$publicHealthUrl = "$publicBaseUrl/health"
$verified = $false
$verifyDeadline = (Get-Date).AddSeconds($WaitSeconds)
do {
    try {
        $verified = Test-HealthResponse -Uri $publicHealthUrl
    }
    catch {
        $verified = $false
    }
    if (-not $verified) {
        Start-Sleep -Milliseconds 750
    }
} while (-not $verified -and (Get-Date) -lt $verifyDeadline)

if (-not $verified) {
    throw "HTTPS 隧道已创建，但远程健康检查失败：$publicHealthUrl"
}

$processId = if ($null -ne $startedProcess) { $startedProcess.Id } elseif (Test-Path -LiteralPath $pidPath) {
    [int](Get-Content -LiteralPath $pidPath -Raw)
}
else {
    $null
}

$metadata = [ordered]@{
    provider = 'cpolar'
    processId = $processId
    localBaseUrl = "http://127.0.0.1:$Port/api"
    publicOrigin = $publicOrigin
    publicBaseUrl = $publicBaseUrl
    publicHealthUrl = $publicHealthUrl
    verifiedAt = (Get-Date).ToString('o')
}

Set-Content -LiteralPath $urlPath -Value $publicBaseUrl -Encoding utf8
$metadata | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $metadataPath -Encoding utf8

Write-Host 'cpolar HTTPS 隧道已就绪。' -ForegroundColor Green
Write-Host "前端 BASE_URLS.tunnel：$publicBaseUrl"
Write-Host "健康检查：$publicHealthUrl"
Write-Host "微信开发者工具也可以执行：wx.setStorageSync('backendBaseUrl', '$publicBaseUrl')"
Write-Host "运行信息：$metadataPath"
