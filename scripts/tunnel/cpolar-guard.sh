#!/bin/bash
# cpolar 隧道守护脚本
# 功能：启动 cpolar → 提取域名 → 更新 request.js → 监控存活
# 用法：bash cpolar-guard.sh

REQUEST_JS="D:/Mythings/software/科研实训/special-education-mvp/miniprogram/utils/request.js"
TUNNEL_SCRIPT="D:/Mythings/software/科研实训/special-education-mvp/scripts/tunnel/start-cpolar-tunnel.ps1"
HEALTH_URL=""

update_request_js() {
  local url="$1"
  # 替换 request.js 中的 tunnel 地址
  local tmp=$(mktemp)
  sed -E "s|tunnel: 'https://[^']+'|tunnel: '$url'|" "$REQUEST_JS" > "$tmp"
  mv "$tmp" "$REQUEST_JS"
  echo "[guard] request.js updated: $url"
}

start_tunnel() {
  echo "[guard] starting cpolar..."
  local output
  output=$(powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$TUNNEL_SCRIPT" -Port 3000 -WaitSeconds 30 2>&1)
  local url
  url=$(echo "$output" | grep -oP 'BASE_URLS\.tunnel：\Khttps://[^[:space:]]+')
  if [ -z "$url" ]; then
    echo "[guard] failed to extract tunnel URL"
    return 1
  fi
  echo "[guard] tunnel: $url"
  HEALTH_URL="${url}/health"
  update_request_js "$url"
  return 0
}

check_health() {
  curl -s --max-time 5 "$HEALTH_URL" > /dev/null 2>&1
}

# main loop
while true; do
  if ! check_health; then
    echo "[guard] tunnel dead, restarting..."
    start_tunnel
    sleep 5
  fi
  sleep 30
done
