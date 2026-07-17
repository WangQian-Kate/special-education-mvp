[CmdletBinding()]
param(
    [string]$BaseUrl
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$savedUrlPath = Join-Path $repoRoot 'work\tunnel\cpolar-base-url.txt'

if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
    if (-not (Test-Path -LiteralPath $savedUrlPath)) {
        throw '没有保存的隧道地址。请先运行 start-cpolar-tunnel.ps1，或使用 -BaseUrl 传入地址。'
    }
    $BaseUrl = Get-Content -LiteralPath $savedUrlPath -Raw
}

$BaseUrl = $BaseUrl.Trim().TrimEnd('/')
if ($BaseUrl -notlike 'https://*') {
    throw '隧道 BASE_URL 必须使用 https://。'
}

$healthUrl = "$BaseUrl/health"
$response = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 15
if ($response.code -ne 0 -or $response.data.status -ne 'UP') {
    throw "隧道响应不符合约定：$($response | ConvertTo-Json -Compress -Depth 6)"
}

Write-Host "HTTPS 联调验证成功：$healthUrl" -ForegroundColor Green
$response | ConvertTo-Json -Depth 6
