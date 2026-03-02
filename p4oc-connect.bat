@echo off
chcp 65001 >nul
setlocal

cd /d %~dp0

echo =============================================
echo    P4OC Remote - Connect to your Phone
echo =============================================
echo.

if not exist "p4oc-client.js" (
    echo Downloading client...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://raw.githubusercontent.com/profitonlineivanov-arch/opencode_access/main/p4oc-client.js', 'p4oc-client.js')"
)

where node >nul
if errorlevel 1 (
    echo Downloading Node.js...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://nodejs.org/dist/v22.20.0/node-v22.20.0-win-x64.zip', 'node.zip')"
    powershell -Command "Expand-Archive -Path 'node.zip' -DestinationPath 'node' -Force"
    xcopy /y node\node.exe . >nul 2>&1
    xcopy /y node\*.dll . >nul 2>&1
    del node.zip >nul 2>&1
    rd /s /q node >nul 2>&1
)

if not exist "node_modules\ws" (
    echo Installing WebSocket module...
    call npm install ws >nul 2>&1
)

start "" opencode serve --port 4096
timeout /t 4 /nobreak

start "" node p4oc-client.js
timeout /t 3 /nobreak

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
echo Keep this window open!
echo Press Enter to exit
echo =============================================
pause
