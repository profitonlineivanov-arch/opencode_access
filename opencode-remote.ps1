# OpenCode Remote - PowerShell Script для Windows
# Сохраните как opencode-remote.ps1 и запустите

Write-Host "🚀 Запуск OpenCode Remote..." -ForegroundColor Cyan

# Проверка наличия opencode
$opencodePath = Get-Command opencode -ErrorAction SilentlyContinue
if (-not $opencodePath) {
    Write-Host "❌ OpenCode не установлен. Установите: https://opencode.ai" -ForegroundColor Red
    exit 1
}

# Проверка/установка cloudflared
$cloudflaredPath = Get-Command cloudflared -ErrorAction SilentlyContinue
if (-not $cloudflaredPath) {
    Write-Host "📦 Установка cloudflared..." -ForegroundColor Yellow
    $tempDir = $env:TEMP
    Invoke-WebRequest -Uri "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe" -OutFile "$tempDir\cloudflared.exe"
    $env:PATH = "$tempDir;$env:PATH"
}

Write-Host "▶️  Запуск OpenCode сервера..." -ForegroundColor Cyan
Start-Process -FilePath "opencode" -ArgumentList "serve","--port","4096" -WindowStyle Hidden
Start-Sleep -Seconds 2

Write-Host "🌐 Создание туннеля..." -ForegroundColor Cyan
Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "✅ ПОДКЛЮЧЕНО!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""
Write-Host "📱 Откройте приложение P4OC на смартфоне" -ForegroundColor White
Write-Host "   и введите эту ссылку:" -ForegroundColor White
Write-Host ""

# Запуск cloudflared и получение URL
$process = Start-Process -FilePath "cloudflared" -ArgumentList "tunnel","--url","localhost:4096" -NoNewWindow -PassThru -RedirectStandardOutput "$env:TEMP\cloudflared.log"

# Ожидание URL
$url = ""
$attempts = 0
while ($url -eq "" -and $attempts -lt 30) {
    Start-Sleep -Seconds 1
    if (Test-Path "$env:TEMP\cloudflared.log") {
        $content = Get-Content "$env:TEMP\cloudflared.log" -Raw -ErrorAction SilentlyContinue
        if ($content -match 'https://[a-z0-9-]+\.trycloudflare\.com') {
            $url = $Matches[0]
        }
    }
    $attempts++
}

if ($url) {
    Write-Host "   $url" -ForegroundColor Yellow -BackgroundColor DarkGray
    Write-Host ""
    Write-Host "==================================" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "Нажмите Ctrl+C для остановки" -ForegroundColor Gray
    Write-Host ""
    
    # Ожидание
    while ($true) { Start-Sleep -Seconds 1 }
} else {
    Write-Host "❌ Не удалось создать туннель" -ForegroundColor Red
}

# Очистка при выходе
Stop-Process -Name "cloudflared" -ErrorAction SilentlyContinue
Stop-Process -Name "opencode" -ErrorAction SilentlyContinue
