[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$stateDirectory = Join-Path $repoRoot 'work\tunnel'
$pidPath = Join-Path $stateDirectory 'cpolar.pid'
$urlPath = Join-Path $stateDirectory 'cpolar-base-url.txt'
$metadataPath = Join-Path $stateDirectory 'cpolar-tunnel.json'

if (-not (Test-Path -LiteralPath $pidPath)) {
    Write-Host '没有发现由项目脚本启动的 cpolar 进程。'
    exit 0
}

$processId = [int](Get-Content -LiteralPath $pidPath -Raw)
$process = Get-Process -Id $processId -ErrorAction SilentlyContinue
if ($null -ne $process) {
    if ($process.ProcessName -ne 'cpolar') {
        throw "PID $processId 当前属于 $($process.ProcessName)，为避免误停进程，操作已取消。"
    }
    Stop-Process -Id $processId
    [void]$process.WaitForExit(5000)
    Write-Host "cpolar 进程已停止：PID $processId" -ForegroundColor Green
}
else {
    Write-Host "cpolar 进程已经退出：PID $processId"
}

foreach ($path in @($pidPath, $urlPath, $metadataPath)) {
    if (Test-Path -LiteralPath $path) {
        Remove-Item -LiteralPath $path -Force
    }
}
