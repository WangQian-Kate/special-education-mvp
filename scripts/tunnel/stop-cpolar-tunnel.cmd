@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-cpolar-tunnel.ps1" %*
exit /b %ERRORLEVEL%
