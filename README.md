# P4OC - Pocket for OpenCode

Native Android клиент для удалённого подключения к OpenCode серверу.

## Возможности

- Подключение к OpenCode серверу по HTTP
- Чат с реальным временем (SSE стриминг)
- Просмотр файлов проекта
- Просмотр кода с подсветкой синтаксиса и номерами строк
- Подтверждение действий (approve/deny tool calls)
- Настройка сервера и авторизации
- Тёмная/светлая тема

## Требования

- JDK 17+
- Android SDK 34
- Gradle 8.x

## Сборка

1. Установите JDK 17: https://adoptium.net/
2. Установите Android SDK: https://developer.android.com/studio
3. Настройте ANDROID_HOME

```bash
# Linux/Mac
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Windows
set ANDROID_HOME=C:\path\to\android-sdk
```

4. Соберите проект:

```bash
# Linux/Mac
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

APK появится в `app/build/outputs/apk/debug/`

## Структура проекта

```
app/src/main/java/dev/p4oc/
├── data/                    # Data layer
│   ├── api/                 # Retrofit API & SSE client
│   ├── model/               # DTOs
│   └── repository/         # Repository implementations
├── di/                      # Hilt dependency injection
├── domain/                  # Domain layer
│   ├── model/               # Domain models
│   └── repository/          # Repository interfaces
└── presentation/           # Presentation layer
    ├── ui/
    │   ├── components/     # Reusable UI components
    │   ├── screens/        # Screen composables
    │   └── theme/          # Material 3 theming
    └── viewmodel/          # ViewModels
```

## Использование

1. Запустите OpenCode на компьютере с флагами для сетевого доступа:
   ```
   opencode web --hostname 0.0.0.0
   ```

2. Установите APK на Android устройство

3. В приложении:
   - Перейдите в Settings
   - Введите IP адрес компьютера и порт (по умолчанию 4096)
   - При необходимости введите логин/пароль
   - Нажмите Save

4. Вернитесь в Chat и нажмите Connect

## Лицензия

MIT
