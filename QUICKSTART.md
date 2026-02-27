# OpenCode Access - Инструкция по запуску

## Быстрый старт (рекомендуется)

### Шаг 1: Запустите OpenCode и настройте туннель

Скопируйте и вставьте эту команду в терминал:

```bash
# Запустите cloudflared туннель (один раз, в фоне)
cloudflared tunnel --protocol http2 --url http://localhost:4096
```

Когда туннель запустится, он покажет URL вида:
```
https://your-tunnel.trycloudflare.com
```

### Шаг 2: Создайте QR-код

Создайте QR-код с этим URL. Добавьте в OpenCode:

```
Создай QR-код с текстом: https://ВАШ_TUNNEL_URL
Покажи его в терминале
```

### Шаг 3: Установите приложение

1. Скачайте APK: https://github.com/profitonlineivanov-arch/opencode_access/releases/download/v1.1.0/app-debug.apk
2. Установите на телефон (разрешите установку из неизвестных источников)
3. Откройте приложение
4. Нажмите "Scan QR Code"
5. Отсканируйте QR-код с экрана

Готово! Приложение автоматически подключится.

---

## Альтернативный способ (ручное подключение)

Если QR-сканер не работает:

1. В приложении нажмите "Manual Connect"
2. Введите URL туннеля (https://xxx.trycloudflare.com)
3. Нажмите "Connect"

---

## Устранение проблем

### Туннель не запускается
```bash
# Проверьте, что cloudflared установлен
cloudflared --version

# Если нет, установите:
# macOS: brew install cloudflared
# Windows: скачайте с https://github.com/cloudflare/cloudflared/releases
```

### Приложение не подключается
- Убедитесь что OpenCode запущен
- Проверьте что туннель активен
- Попробуйте пересканировать QR-код
