@echo off
chcp 65001 >nul
title P4OC Tunnel

echo.
echo =============================================
echo    P4OC - Connect Your Phone to OpenCode
echo =============================================
echo.

setlocal enabledelayedexpansion

REM Check if cloudflared exists
if not exist "cloudflared.exe" (
    echo Downloading cloudflared...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'" >nul 2>&1
    if not exist "cloudflared.exe" (
        echo ERROR: Could not download cloudflared
        pause
        exit /b 1
    )
)

REM Kill old processes
taskkill /F /IM cloudflared.exe 2>nul
del tunnel.log 2>nul

REM Get local IP
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /C:"IPv4"') do set LOCAL_IP=%%a
set LOCAL_IP=%LOCAL_IP:~1%

echo.
echo OPTION 1: Local Network (RECOMMENDED)
echo.
echo Your PC IP: %LOCAL_IP%
echo.
echo On your phone:
echo 1. Open P4OC app
echo 2. Go to Settings
echo 3. Enter IP: %LOCAL_IP%  Port: 4096
echo.
echo =============================================
echo.

echo Starting OpenCode server...
start /B opencode serve --port 4096 --hostname 0.0.0.0
timeout /t 3 /nobreak >nul

echo Creating tunnel...
start /B cmd /c "cloudflared.exe tunnel --protocol http2 --url localhost:4096 > tunnel.log 2>&1"

echo Waiting for tunnel...
timeout /t 20 /nobreak >nul

set TUNNEL_URL=
for /f "tokens=*" %%a in ('type tunnel.log 2^>nul ^| findstr /C:"trycloudflare.com"') do set TUNNEL_URL=%%a

if defined TUNNEL_URL (
    for /f "tokens=2 delims=|" %%a in ("!TUNNEL_URL!") do set CLEAN_URL=%%a
    if not defined CLEAN_URL set CLEAN_URL=!TUNNEL_URL!
    
    echo.
    echo OPTION 2: Remote Access (Tunnel)
    echo.
    echo Your tunnel URL:
    echo !CLEAN_URL!
    echo.
    echo Copy this URL and paste in P4OC app Settings
    echo.
    echo Opening QR code...
    start "" "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=!CLEAN_URL!"
    echo.
) else (
    echo.
    echo Could not create tunnel. Use local network option above.
    echo.
)

echo =============================================
echo Press any key to close
pause >nul
