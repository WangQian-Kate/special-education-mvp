[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 测试 1: WebClient
try {
  $wc = New-Object System.Net.WebClient
  $wc.Headers.Add("x-api-key", "test")
  $wc.Headers.Add("content-type", "application/json; charset=utf-8")
  $result = $wc.UploadString("https://open.bigmodel.cn/api/anthropic/v1/messages", "POST", "{}")
  Write-Host "WebClient works: $result"
} catch {
  Write-Host "WebClient error: $($_.Exception.Message)"
}

# 测试 2: HttpWebRequest with explicit Uri
try {
  $uriObj = New-Object System.Uri("https://open.bigmodel.cn/api/anthropic/v1/messages")
  Write-Host "Uri created: $($uriObj.ToString())"
  $req = [System.Net.WebRequest]::Create($uriObj)
  Write-Host "Request type: $($req.GetType().FullName)"
  $req.Method = "POST"
  $req.ContentType = "application/json; charset=utf-8"
  Write-Host "HttpWebRequest works"
} catch {
  Write-Host "HttpWebRequest error: $($_.Exception.Message)"
}

# 测试 3: Invoke-WebRequest Content property
try {
  $resp = Invoke-WebRequest -Method Post -Uri "https://open.bigmodel.cn/api/anthropic/v1/messages" -Headers @{"x-api-key"="test"} -Body '{}' -UseBasicParsing
  Write-Host "IWR Content type: $($resp.Content.GetType().FullName)"
  Write-Host "IWR RawContentStream: $($resp.RawContentStream)"
} catch {
  Write-Host "IWR error (expected with bad key): $($_.Exception.Message)"
}
