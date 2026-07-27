@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0create-teacher-binding-code.ps1" %*
