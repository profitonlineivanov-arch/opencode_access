# Подключение телефона к OpenCode (30 секунд)

## Скажи это агенту (скопируй и отправь):

```
Пожалуйста, сделай следующее:
1. Открой терминал
2. Вставь и выполни эту команду:
cloudflared tunnel --protocol http2 --url http://localhost:4096
3. Дождись когда появится ссылка (например https://xxx.trycloudflare.com)
4. Создай QR-код с этой ссылкой и покажи мне

Если cloudflared не установлен - установи его: https://github.com/cloudflare/cloudflared/releases
```

## Всё!

Агент сам сделает туннель и покажет QR-код.
Тебе нужно только отсканировать его приложением.

---

## Если нужно ещё проще

Просто дай агенту эту ссылку:
**https://github.com/cloudflare/cloudflared/releases**

И скажи: "Скачай, установи и запусти туннель командой:
cloudflared tunnel --protocol http2 --url http://localhost:4096

Потом покажи QR-код"
