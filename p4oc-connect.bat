@echo off
chcp 65001 >nul
title P4OC Tunnel

echo.
echo === P4OC Remote ===
echo.

if not exist "cloudflared.exe" (
    echo Downloading cloudflared...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'"
)

taskkill /F /IM cloudflared.exe 2>nul

echo Starting OpenCode server...
start /B opencode serve --port 4096 >nul 2>&1
timeout /t 3 /nobreak >nul

echo Creating tunnel...
cloudflared.exe tunnel --url localhost:4096 > tunnel.log 2>&1 &

echo Waiting for URL...
timeout /t 8 /nobreak >nul

for /f "delims=" %%a in ('findstr "trycloudflare" tunnel.log 2^>nul') do set URL=%%a

if defined URL (
    echo.
    echo ========================================
    echo YOUR CONNECTION URL:
    echo %URL%
    echo ========================================
    echo.
    echo Opening QR code...
    
    powershell -Command "Start-Process 'https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=%URL%'"
    
) else (
    echo Could not get tunnel URL. Check tunnel.log
)

echo.
echo Scan QR code with P4OC app on your phone!
echo Press any key to exit
pause >nul
