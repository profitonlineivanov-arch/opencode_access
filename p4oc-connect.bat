@echo off
chcp 65001 >nul
title P4OC Tunnel

echo.
echo === P4OC Remote ===
echo.

setlocal enabledelayedexpansion

if not exist "cloudflared.exe" (
    echo Downloading cloudflared...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'"
)

taskkill /F /IM cloudflared.exe 2>nul

echo Starting OpenCode server...
start /B opencode serve --port 4096
timeout /t 3 /nobreak >nul

echo Creating secure tunnel...
start /B cmd /c "cloudflared.exe tunnel --url localhost:4096 > tunnel.log 2>&1"

echo Waiting for URL...
timeout /t 12 /nobreak >nul

set URL=
for /f "tokens=*" %%a in ('type tunnel.log 2^>nul ^| findstr /C:"trycloudflare.com"') do set URL=%%a

if not defined URL (
    timeout /t 8 /nobreak >nul
    for /f "tokens=*" %%a in ('type tunnel.log 2^>nul ^| findstr /C:"trycloudflare.com"') do set URL=%%a
)

if defined URL (
    cls
    echo.
    echo ========================================
    echo    READY TO CONNECT!
    echo ========================================
    echo.
    echo Scan this QR code with P4OC app:
    echo.
    for /f "tokens=2 delims=|" %%a in ("!URL!") do set CLEAN_URL=%%a
    if not defined CLEAN_URL set CLEAN_URL=!URL!
    start "" "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=!CLEAN_URL!"
    echo.
    echo URL: !CLEAN_URL!
    echo.
    echo ========================================
    echo Press any key to close
    pause >nul
) else (
    echo.
    echo Could not get tunnel URL. Check tunnel.log
    type tunnel.log 2>nul
    echo.
    pause
)
