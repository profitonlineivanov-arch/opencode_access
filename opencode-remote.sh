#!/bin/bash

# OpenCode Remote Connect Script
# Одна команда для запуска туннеля и получения QR-кода

set -e

echo "🚀 Запуск OpenCode Remote..."

# Проверка наличия opencode
if ! command -v opencode &> /dev/null; then
    echo "❌ OpenCode не установлен. Установите: https://opencode.ai"
    exit 1
fi

# Проверка наличия cloudflared
if ! command -v cloudflared &> /dev/null; then
    echo "📦 Установка cloudflared..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install cloudflared
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        curl -sL https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /tmp/cloudflared
        chmod +x /tmp/cloudflared
        export PATH="/tmp:$PATH"
    else
        echo "❌ Windows не поддерживается. Используйте WSL или скачайте cloudflared вручную:"
        echo "   https://github.com/cloudflare/cloudflared/releases"
        exit 1
    fi
fi

# Остановка предыдущего туннеля если есть
pkill -f "cloudflared tunnel" 2>/dev/null || true

echo "▶️  Запуск OpenCode сервера..."
opencode serve --port 4096 &
OPENCODE_PID=$!

sleep 2

echo "🌐 Создание туннеля..."
cloudflared tunnel --url localhost:4096 2>&1 | while read line; do
    if echo "$line" | grep -q "trycloudflare.com"; then
        URL=$(echo "$line" | grep -o 'https://[^ ]*\.trycloudflare\.com' | head -1)
        if [ -n "$URL" ]; then
            echo ""
            echo "═══════════════════════════════════════"
            echo "✅ ПОДКЛЮЧЕНО!"
            echo "═══════════════════════════════════════"
            echo ""
            echo "📱 Откройте приложение P4OC на смартфоне"
            echo "   и введите эту ссылку:"
            echo ""
            echo "   $URL"
            echo ""
            
            # Генерация QR кода
            if command -v qrencode &> /dev/null; then
                echo "📱 Или отсканируйте QR-код:"
                echo ""
                qrencode -s 10 -t ANSI "$URL"
            fi
            
            echo ""
            echo "═══════════════════════════════════════"
            echo "Нажмите Ctrl+C для остановки"
            echo "═══════════════════════════════════════"
        fi
    fi
done

# Очистка при выходе
trap "kill $OPENCODE_PID 2>/dev/null; pkill -f 'cloudflared tunnel' 2>/dev/null" EXIT
