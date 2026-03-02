@echo off
chcp 65001 >nul
title P4OC Remote

echo.
echo =============================================
echo    P4OC Remote - Connect to your Phone
echo =============================================
echo.

echo Starting OpenCode server...
start /B opencode serve --port 4096
timeout /t 4 /nobreak >nul

echo Creating SSH tunnel to proxy server...
start /B ssh -N -R 8096:localhost:4096 root@45.146.164.144

echo Waiting for connection...
timeout /t 5 /nobreak >nul

cls
echo.
echo =============================================
echo    READY TO CONNECT!
echo =============================================
echo.
echo On your phone:
echo 1. Open P4OC app
echo 2. Settings - turn OFF "Use URL"
echo 3. IP: 45.146.164.144
echo 4. Port: 8096
echo 5. Save and Connect
echo.
echo =============================================
echo Press any key to close tunnel
pause >nul
