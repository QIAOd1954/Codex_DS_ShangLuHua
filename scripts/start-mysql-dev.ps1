$env:JAVA_HOME='E:\JAVAjdk17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
Set-Location (Resolve-Path "$PSScriptRoot\..")
Write-Host "Starting with MySQL profile. Default database: localhost:3306/shangluhua, user root, password 123456."
Write-Host "If needed, set `$env:MYSQL_PASSWORD before running this script."
mvn spring-boot:run -Dspring-boot.run.profiles=mysql