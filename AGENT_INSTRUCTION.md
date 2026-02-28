# Инструкция для агента OpenCode

## Задача
Пользователю нужен QR-код для подключения телефона к твоему серверу.

## Что делать

### Шаг 1: Проверь есть ли cloudflared
В терминале введи: `cloudflared --version`

Если нет - скачай: https://github.com/cloudflare/cloudflared/releases

### Шаг 2: Запусти туннель
Введи и выполни:
```
cloudflared tunnel --protocol http2 --url http://localhost:4096
```

### Шаг 3: Дождись URL
Терминал покажет ссылку вида: `https://xxx.trycloudflare.com`

### Шаг 4: Создай QR-код
Введи (подставив свою ссылку):
```
python -c "import qrcode; qrcode.make('https://твоя_ссылка.trycloudflare.com').save('qr.png')"
```

### Шаг 5: Покажи QR-код
Введи `start qr.png` (Windows) или `open qr.png` (Mac)

## Готово!
Скажи пользователю: "Отсканируй QR-код приложением"
