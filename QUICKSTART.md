# Как подключить телефон к OpenCode (2 минуты)

## Шаг 1: Скажите агенту это:

```
Пожалуйста, сделай следующее:
1. Запусти в терминале: cloudflared tunnel --protocol http2 --url http://localhost:4096
2. Когда получишь URL туннеля - создай QR-код с этим URL и покажи его мне
```

## Шаг 2: Отсканируйте QR-код

1. Установите APK: https://github.com/profitonlineivanov-arch/opencode_access/releases/download/v1.1.0/app-debug.apk
2. Откройте приложение
3. Нажмите "Scan QR Code"
4. Наведите камеру на QR-код

## Готово!

Приложение автоматически подключится к OpenCode.

---

## Если что-то не работает

### "cloudflared не найден"
Скажите агенту: "Установи cloudflared: https://github.com/cloudflare/cloudflared/releases"

### "QR-кода нет на экране"
Скажите агенту: "Создай QR-код с URL туннеля командой: python -c \"import qrcode; qrcode.make('URL').save('qr.png')\" и покажи картинку"

### Туннель закрылся
Просто попросите перезапустить туннель и создать новый QR-код
