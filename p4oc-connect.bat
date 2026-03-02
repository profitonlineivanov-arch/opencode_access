@echo off
chcp 65001 >nul
title P4OC Remote

echo.
echo =============================================
echo    P4OC Remote - Connect to your Phone
echo =============================================
echo.

REM Check if Node.js is installed
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed!
    echo Please install Node.js from https://nodejs.org
    echo.
    pause
    exit /b 1
)

REM Install ws module if needed
if not exist "node_modules\ws" (
    echo Installing dependencies...
    call npm install ws
)

echo Starting OpenCode server...
start /B opencode serve --port 4096
timeout /t 4 /nobreak >nul

echo Connecting to proxy server...
start /B node p4oc-client.js

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
echo Press any key to exit
pause >nul
