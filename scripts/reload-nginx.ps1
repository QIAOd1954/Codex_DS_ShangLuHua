$nginx = 'H:\Nginx\nginx-1.20.2\nginx.exe'
if (-not (Test-Path -LiteralPath $nginx)) {
  Write-Error "Nginx 涓嶅瓨鍦細$nginx"
  exit 1
}
& $nginx -s reload
