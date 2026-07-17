@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0test-cpolar-tunnel.ps1" %*
exit /b %ERRORLEVEL%
