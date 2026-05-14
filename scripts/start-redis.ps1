$redis = 'H:\Redis-x64-3.2.100\start-redis.bat'
if (-not (Test-Path -LiteralPath $redis)) {
  Write-Error "Redis 鍚姩鑴氭湰涓嶅瓨鍦細$redis"
  exit 1
}
Start-Process -FilePath $redis -WorkingDirectory (Split-Path -Parent $redis)
Write-Host "Redis 鍚姩鍛戒护宸插彂閫侊細$redis"
