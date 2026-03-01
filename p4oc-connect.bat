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
    if not exist "cloudflared.exe" (
        echo Download failed. Trying alternate method...
        powershell -Command "Start-Process 'https://github.com/cloudflare/cloudflared/releases/latest/cloudflared-windows-amd64.exe/download'"
        echo.
        echo Please download cloudflared manually and save in the same folder as this script.
        echo.
        pause
        exit
    )
)

taskkill /F /IM cloudflared.exe 2>nul
del tunnel.log 2>nul

echo Starting OpenCode server...
start /B opencode serve --port 4096
timeout /t 4 /nobreak >nul

echo Creating secure tunnel...
start /B cmd /c "cloudflared.exe tunnel --url localhost:4096 2^>^&1 > tunnel.log"

echo Waiting for connection...
timeout /t 15 /nobreak >nul

set URL=
for /f "tokens=*" %%a in ('findstr /C:"trycloudflare" tunnel.log 2^>nul') do set URL=%%a

if not defined URL (
    timeout /t 10 /nobreak >nul
    for /f "tokens=*" %%a in ('findstr /C:"trycloudflare" tunnel.log 2^>nul') do set URL=%%a
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
    echo Could not create tunnel. Try running as Administrator.
    echo.
    pause
)
