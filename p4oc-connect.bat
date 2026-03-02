@echo off
chcp 65001 >nul
title P4OC Remote

echo.
echo =============================================
echo    P4OC Remote - Connect to your Phone
echo =============================================
echo.

REM Check for Node.js portable or installed
where node >nul 2>&1
if %errorlevel% equ 0 goto :start

REM Try to download Node.js portable
echo Node.js not found. Downloading portable version...
mkdir node_temp 2>nul
powershell -Command "Invoke-WebRequest -Uri 'https://nodejs.org/dist/v22.20.0/node-v22.20.0-win-x64.zip' -OutFile 'node_temp\node.zip'"
powershell -Command "Expand-Archive -Path 'node_temp\node.zip' -DestinationPath 'node_temp' -Force"
move node_temp\node-v22.20.0-win-x64\node.exe . >nul 2>&1
move node_temp\node-v22.20.0-win-x64\*.dll . >nul 2>&1
rmdir /s /q node_temp 2>nul

:start
echo Starting OpenCode server...
start /B opencode serve --port 4096
timeout /t 4 /nobreak >nul

echo Connecting to proxy server...
if exist node.exe (
    start /B node p4oc-client.js
) else (
    echo ERROR: Cannot connect to server without Node.js
    pause
    exit /b 1
)

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
