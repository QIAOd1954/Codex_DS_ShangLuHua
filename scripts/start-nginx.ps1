$nginx = 'H:\Nginx\nginx-1.20.2\nginx.exe'
if (-not (Test-Path -LiteralPath $nginx)) {
  Write-Error "Nginx 涓嶅瓨鍦細$nginx"
  exit 1
}
Start-Process -FilePath $nginx -WorkingDirectory (Split-Path -Parent $nginx) -WindowStyle Hidden
Write-Host "Nginx 鍚姩鍛戒护宸插彂閫侊細$nginx"
