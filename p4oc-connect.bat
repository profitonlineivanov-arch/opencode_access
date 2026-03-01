@echo off
chcp 65001 >nul
title P4OC Tunnel

echo.
echo === P4OC Remote ===
echo.

if not exist "cloudflared.exe" (
    echo Downloading cloudflared...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'" >nul 2>&1
)

taskkill /F /IM cloudflared.exe 2>nul

echo Starting OpenCode server...
start /B opencode serve --port 4096 >nul 2>&1
timeout /t 3 /nobreak >nul

echo Creating secure tunnel...
start /B cloudflared.exe tunnel --url localhost:4096 >nul 2>&1

echo Getting connection URL...
timeout /t 10 /nobreak >nul

for /f "delims=" %%a in ('findstr /C:"trycloudflare" tunnel.log 2^>nul') do set URL=%%a

if not defined URL (
    timeout /t 5 /nobreak >nul
    for /f "delims=" %%a in ('findstr /C:"trycloudflare" tunnel.log 2^>nul') do set URL=%%a
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
    start "" "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=%URL%"
    echo.
    echo Or copy this URL to your phone:
    echo %URL%
    echo.
    echo ========================================
    echo Press any key to close
    pause >nul
) else (
    echo.
    echo Could not create tunnel. Make sure you have internet connection.
    echo.
    pause
)
