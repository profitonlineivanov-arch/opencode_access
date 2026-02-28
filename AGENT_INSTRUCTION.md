# Инструкция для агента OpenCode

## Задача

Пользователю нужно подключить Android-приложение к твоему серверу. Нужно создать QR-код с URL туннеля.

## Выполни эти шаги:

### Шаг 1: Запусти туннель

Выполни в терминале:
```
cloudflared tunnel --protocol http2 --url http://localhost:4096
```

**Важно:** Дождись пока появится URL (например `https://xxx.trycloudflare.com`)

### Шаг 2: Создай QR-код

После получения URL выполни:
```
python -c "import qrcode; qrcode.make('ТВОЙ_URL').save('qr.png')"
```

### Шаг 3: Покажи QR-код

Открой файл qr.png командой:
```
# Windows
start qr.png

# macOS
open qr.png

# Linux
xdg-open qr.png
```

### Шаг 4: Подтверди

Скажи пользователю: "Готово! Отсканируй QR-код приложением"

---

## Если cloudflared не установлен

**Windows (PowerShell):**
```
Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'
```

**macOS:**
```
brew install cloudflared
```

**Linux:**
```
curl -sL https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o cloudflared
chmod +x cloudflared
```

---

## Важные правила:

1. ✅ Всегда используй флаг `--protocol http2`
2. ✅ Не завершай туннель пока пользователь не отключится
3. ✅ При перезапуске туннеля нужно создать новый QR-код
4. ❌ Не говори "я не могу" - просто сделай это
5. ❌ Не проси пользователя сделать что-то самому - сделай сам
