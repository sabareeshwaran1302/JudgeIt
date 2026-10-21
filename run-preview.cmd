@echo off
setlocal
cd /d "%~dp0"
call mvnw.cmd spring-boot:run -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.profiles=preview %*
