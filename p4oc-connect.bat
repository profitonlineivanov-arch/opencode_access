@echo off
chcp 65001 >nul
title P4OC Remote

echo.
echo =============================================
echo    P4OC Remote - Connect to your Phone
echo =============================================
echo.

REM Set working directory to where the bat is located
cd /d %~dp0

REM Download client script if not exists
if not exist "p4oc-client.js" (
    echo Downloading client...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/profitonlineivanov-arch/opencode_access/main/p4oc-client.js' -OutFile 'p4oc-client.js'"
)

REM Check for Node.js
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo Downloading Node.js (one time)...
    powershell -Command "Invoke-WebRequest -Uri 'https://nodejs.org/dist/v22.20.0/node-v22.20.0-win-x64.zip' -OutFile 'node.zip'"
    powershell -Command "Expand-Archive -Path 'node.zip' -DestinationPath 'node' -Force"
    copy /y node\node.exe . >nul 2>&1
    copy /y node\*.dll . >nul 2>&1
    del node.zip >nul 2>&1
    rmdir /s /q node >nul 2>&1
)

REM Install ws module
if not exist "node_modules\ws" (
    echo Installing WebSocket module...
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
