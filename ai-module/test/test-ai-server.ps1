[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$jsonPath = Join-Path $scriptDir ".." "mock" "ai001-aligned-input.json"
$body = Get-Content $jsonPath -Raw -Encoding UTF8
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)

[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
$wc = New-Object System.Net.WebClient
$wc.Headers.Add("Content-Type", "application/json; charset=utf-8")

try {
  $respBytes = $wc.UploadData("http://localhost:3001/api/ai/report", "POST", $bodyBytes)
  $respText = [System.Text.Encoding]::UTF8.GetString($respBytes)
  Write-Host $respText
} catch {
  Write-Host "ERROR: $($_.Exception.Message)"
  $inner = $_.Exception.InnerException
  if ($inner -and $inner.Response) {
    $stream = $inner.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
    Write-Host $reader.ReadToEnd()
  }
}