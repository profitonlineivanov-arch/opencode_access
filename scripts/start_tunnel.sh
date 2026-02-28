#!/bin/bash

# Скрипт для запуска туннеля и создания QR-кода
# Просто запусти этот файл и следуй инструкциям

echo "🚀 Запускаю туннель..."

# Проверка и установка cloudflared
if ! command -v cloudflared &> /dev/null; then
    echo "📥 Устанавливаю cloudflared..."
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        if command -v brew &> /dev/null; then
            brew install cloudflared
        else
            echo "Установи Homebrew: https://brew.sh"
            exit 1
        fi
    elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        # Windows
        echo "Скачиваю cloudflared для Windows..."
        curl -sL "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe" -o cloudflared.exe
        CLOUDFLARED="./cloudflared.exe"
    else
        # Linux
        echo "Скачиваю cloudflared..."
        curl -sL "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64" -o cloudflared
        chmod +x cloudflared
        CLOUDFLARED="./cloudflared"
    fi
else
    CLOUDFLARED="cloudflared"
fi

# Запуск туннеля в фоне
$CLOUDFLARED tunnel --protocol http2 --url http://localhost:4096 &

# Ожидание URL
echo "⏳ Жду URL туннеля..."
sleep 5

# Поиск URL в выводе
TUNNEL_URL=""
for i in {1..30}; do
    TUNNEL_URL=$(ps aux | grep cloudflared | grep -o 'https://[^ ]*\.trycloudflare\.com' | head -1)
    if [ -n "$TUNNEL_URL" ]; then
        break
    fi
    sleep 1
done

if [ -z "$TUNNEL_URL" ]; then
    echo "❌ Не удалось получить URL туннеля"
    exit 1
fi

echo "✅ URL получен: $TUNNEL_URL"

# Создание QR-кода
echo "📱 Создаю QR-код..."

if command -v python3 &> /dev/null; then
    python3 -c "import qrcode; qrcode.make('$TUNNEL_URL').save('qrcode.png'); print('QR-код сохранён в файл qrcode.png')"
    echo "📂 Открой файл qrcode.png и покажи его мне"
elif command -v python &> /dev/null; then
    python -c "import qrcode; qrcode.make('$TUNNEL_URL').save('qrcode.png'); print('QR-код сохранён в файл qrcode.png')"
    echo "📂 Открой файл qrcode.png и покажи его мне"
elif command -v node &> /dev/null; then
    npx qrcode "$TUNNEL_URL"
else
    echo "QR код: $TUNNEL_URL"
    echo "Установи Python или Node.js для создания QR-кода"
fi

echo ""
echo "✨ Готово! Сканируй QR-код приложением Opencode Access"
