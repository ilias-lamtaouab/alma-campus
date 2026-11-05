@echo off
cd /d "%~dp0"
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot
echo Compiling and launching Scolaris...
call mvnw.cmd compile javafx:run
pause
