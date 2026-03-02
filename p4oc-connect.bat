@echo off
chcp 65001 >nul
title P4OC Remote

echo.
echo =============================================
echo    P4OC Remote - Connect to your Phone
echo =============================================
echo.

setlocal enabledelayedexpansion

cd /d %~dp0

if not exist "p4oc-client.js" (
    echo Downloading client...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/profitonlineivanov-arch/opencode_access/main/p4oc-client.js' -OutFile 'p4oc-client.js'"
)

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo Downloading Node.js...
    powershell -Command "Invoke-WebRequest -Uri 'https://nodejs.org/dist/v22.20.0/node-v22.20.0-win-x64.zip' -OutFile 'node.zip'"
    powershell -Command "Expand-Archive -Path 'node.zip' -DestinationPath 'node' -Force"
    copy /y node\node.exe . >nul 2>&1
    copy /y node\*.dll . >nul 2>&1
    del node.zip >nul 2>&1
    rmdir /s /q node >nul 2>&1
)

if not exist "node_modules\ws" (
    echo Installing WebSocket module...
    call npm install ws
)

echo Starting OpenCode server...
start "" opencode serve --port 4096

timeout /t 4 /nobreak >nul

echo Connecting to proxy server...
start "" node p4oc-client.js

timeout /t 3 /nobreak >nul

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
echo Keep this window open while using P4OC
echo Press any key to close
pause >nul
