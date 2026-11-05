@echo off
echo ====================================================
echo      LANCEMENT DE L'APPLICATION SCOLARIS
echo ====================================================
echo.

:: Configuration de Java
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Compilation et demarrage en cours...
echo Cela peut prendre quelques secondes.
echo.

:: Lancement avec le Maven Wrapper
call "%~dp0mvnw.cmd" clean javafx:run

echo.
echo Le programme s'est arrete ou une erreur s'est produite.
pause
