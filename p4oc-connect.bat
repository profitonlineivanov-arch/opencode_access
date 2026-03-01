@echo off
chcp 65001 >nul
title P4OC Tunnel

echo.
echo === P4OC Remote ===
echo.

REM Download cloudflared if not exists
if not exist "cloudflared.exe" (
    echo Downloading cloudflared...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'"
)

echo Starting OpenCode server...
start /B opencode serve --port 4096 >nul 2>&1

timeout /t 3 /nobreak >nul

echo Creating tunnel...
cloudflared.exe tunnel --url localhost:4096
