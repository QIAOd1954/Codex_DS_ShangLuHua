Write-Host "1/3 鍚姩 Redis"
& "$PSScriptRoot\start-redis.ps1"
Write-Host "2/3 鍚姩 Nginx"
& "$PSScriptRoot\start-nginx.ps1"
Write-Host "3/3 鍚姩 Spring Boot锛岄娆¤繍琛屼細涓嬭浇 Maven 渚濊禆"
Set-Location (Resolve-Path "$PSScriptRoot\..")
mvn spring-boot:run
