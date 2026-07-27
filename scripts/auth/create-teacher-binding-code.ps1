param(
    [string]$TeacherId,
    [ValidateRange(5, 1440)]
    [int]$ValidityMinutes = 30,
    [string]$Database = 'special_ed_assistant',
    [string]$DatabaseUser = 'special_ed_app'
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($TeacherId)) {
    $TeacherId = Read-Host '请输入教师编号（例如 t001）'
}
$TeacherId = $TeacherId.Trim()
if ($TeacherId -notmatch '^t\d{3}$') {
    throw '教师编号格式不正确，必须是 t 加 3 位数字，例如 t001。'
}
if ($Database -notmatch '^[A-Za-z0-9_]+$' -or $DatabaseUser -notmatch '^[A-Za-z0-9_]+$') {
    throw '数据库名或数据库用户名包含不允许的字符。'
}

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysql) {
    throw '未找到 mysql.exe，请先将 MySQL bin 目录加入 PATH。'
}

$alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
$plainCode = -join (1..8 | ForEach-Object {
    $alphabet[[System.Security.Cryptography.RandomNumberGenerator]::GetInt32($alphabet.Length)]
})
$displayCode = $plainCode.Substring(0, 4) + '-' + $plainCode.Substring(4, 4)
$hashBytes = [System.Security.Cryptography.SHA256]::HashData(
    [System.Text.Encoding]::UTF8.GetBytes($plainCode)
)
$codeHash = [Convert]::ToHexString($hashBytes).ToLowerInvariant()

$securePassword = Read-Host "请输入数据库用户 $DatabaseUser 的密码" -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $env:MYSQL_PWD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    $userId = & $mysql.Source --default-character-set=utf8mb4 -u $DatabaseUser -N `
        -e "SELECT id FROM $Database.app_user WHERE teacher_id='$TeacherId';"
    if ($LASTEXITCODE -ne 0) {
        throw "查询教师失败，mysql 退出码：$LASTEXITCODE"
    }
    if ([string]::IsNullOrWhiteSpace(($userId | Out-String))) {
        throw "数据库中不存在教师 $TeacherId。"
    }

    & $mysql.Source --default-character-set=utf8mb4 -u $DatabaseUser -e @"
INSERT INTO $Database.teacher_binding_code (user_id, code_hash, expires_at)
VALUES ($($userId.Trim()), '$codeHash', DATE_ADD(NOW(3), INTERVAL $ValidityMinutes MINUTE));
"@
    if ($LASTEXITCODE -ne 0) {
        throw "创建教师绑定码失败，mysql 退出码：$LASTEXITCODE"
    }
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
}

Write-Host ''
Write-Host "教师：$TeacherId"
Write-Host "一次性绑定码：$displayCode"
Write-Host "有效时间：$ValidityMinutes 分钟"
Write-Host '该绑定码只显示一次，使用后立即失效。'
