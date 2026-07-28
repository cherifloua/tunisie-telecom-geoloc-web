@echo off
title Tunisie Telecom - Geolocalisation des sites
color 0C

echo ============================================================
echo   Tunisie Telecom - Geolocalisation des sites
echo   Demarrage de l'application...
echo ============================================================
echo.

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERREUR] Java n'est pas installe sur ce PC.
    echo Installez d'abord Java 17 ou plus recent :
    echo https://adoptium.net/fr/temurin/releases/?version=17
    echo.
    pause
    exit /b 1
)

echo Adresse(s) IP de ce PC sur le reseau ^(pour te connecter depuis ton telephone^) :
echo ------------------------------------------------------------
ipconfig | findstr /R /C:"IPv4"
echo ------------------------------------------------------------
echo.
echo Sur ton telephone ^(connecte au MEME Wi-Fi que ce PC^), ouvre :
echo    http://ADRESSE-IP-CI-DESSUS:8080
echo.
echo Sur ce PC, ouvre :
echo    http://localhost:8080
echo.
echo Premier lancement : le script va telecharger Maven puis les
echo dependances du projet ^(besoin d'internet la 1ere fois
echo seulement^), ca peut prendre quelques minutes. Les fois
echo suivantes seront rapides.
echo.
echo Pour arreter l'application : ferme cette fenetre ou Ctrl+C.
echo ============================================================
echo.

start "" http://localhost:8080
timeout /t 3 >nul

set PORT=8080
call mvnw.cmd spring-boot:run

pause
