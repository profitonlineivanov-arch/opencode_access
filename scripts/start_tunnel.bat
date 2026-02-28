@echo off
chcp 65001 >nul
echo ========================================
echo   Запуск туннеля для Opencode Access
echo ========================================
echo.

REM Проверка cloudflared
where cloudflared >nul 2>&1
if %errorlevel% neq 0 (
    echo 📥 cloudflared не найден, скачиваю...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'"
    set CLOUDFLARED=cloudflared.exe
) else (
    set CLOUDFLARED=cloudflared
)

echo 🚀 Запускаю туннель...
start /B %CLOUDFLARED% tunnel --protocol http2 --url http://localhost:4096 2>tunnel.log

echo ⏳ Жду URL туннеля...
timeout /t 8 /nobreak >nul

REM Получение URL из лога
set TUNNEL_URL=
for /f "delims=" %%a in ('type tunnel.log ^| findstr /i "trycloudflare"') do set TUNNEL_URL=%%a

if "%TUNNEL_URL%"=="" (
    echo ❌ Ошибка: не удалось получить URL
    type tunnel.log
    pause
    exit /b 1
)

echo ✅ URL: %TUNNEL_URL%
echo.

REM Создание QR-кода
echo 📱 Создаю QR-код...
python -c "import qrcode; qrcode.make('%TUNNEL_URL%').save('qrcode.png')" 2>nul

if exist qrcode.png (
    echo ✅ QR-код создан: qrcode.png
    echo 📂 Открой файл qrcode.png и покажи его пользователю
    start qrcode.png
) else (
    echo.
    echo ═════════════════════════════════════
    echo Скажи пользователю этот URL:
    echo %TUNNEL_URL%
    echo ═════════════════════════════════════
)

echo.
echo ✨ Готово! Пусть пользователь отсканирует QR-код
echo.
pause
