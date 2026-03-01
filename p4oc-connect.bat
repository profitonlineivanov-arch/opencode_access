@echo off
chcp 65001 >nul
title P4OC Tunnel

echo.
echo =============================================
echo    P4OC - Connect Your Phone to OpenCode
echo =============================================
echo.

setlocal enabledelayedexpansion

REM Get local IP
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /C:"IPv4"') do set LOCAL_IP=%%a
set LOCAL_IP=%LOCAL_IP:~1%

echo.
echo OPTION 1: Local Network (RECOMMENDED)
echo.
echo Your PC IP: %LOCAL_IP%
echo.
echo On your phone:
echo 1. Open P4OC
echo 2. Settings - turn OFF "Use URL"
echo 3. IP: %LOCAL_IP%   Port: 4096
echo 4. Save and Connect
echo.
echo =============================================
echo.

echo Starting OpenCode server...
start /B opencode serve --port 4096 --hostname 0.0.0.0
timeout /t 3 /nobreak >nul

echo.
echo OPTION 2: Remote Access (try Cloudflare)
echo.
echo Creating tunnel...
start /B cmd /c "cloudflared.exe tunnel --protocol http2 --url localhost:4096 > tunnel.log 2>&1"

echo Waiting...
timeout /t 15 /nobreak >nul

set TUNNEL_URL=
for /f "tokens=*" %%a in ('type tunnel.log 2^>nul ^| findstr /C:"trycloudflare.com"') do set TUNNEL_URL=%%a

if defined TUNNEL_URL (
    for /f "tokens=2 delims=|" %%a in ("!TUNNEL_URL!") do set CLEAN_URL=%%a
    if not defined CLEAN_URL set CLEAN_URL=!TUNNEL_URL!
    echo SUCCESS! Tunnel URL:
    echo !CLEAN_URL!
    echo.
    echo Opening QR code...
    start "" "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=!CLEAN_URL!"
) else (
    echo Cloudflare tunnel failed. Use Option 1 above.
    echo.
    echo Alternative: Try manual tunnel:
    echo 1. Download and install cloudflared
    echo 2. Run: cloudflared.exe tunnel --url localhost:4096
)

echo.
echo =============================================
echo Press any key to close
pause >nul
