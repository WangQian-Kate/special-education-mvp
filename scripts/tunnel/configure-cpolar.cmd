@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0configure-cpolar.ps1" %*
exit /b %ERRORLEVEL%
