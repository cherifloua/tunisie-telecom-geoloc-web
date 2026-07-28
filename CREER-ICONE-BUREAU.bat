@echo off
title Creation du raccourci sur le Bureau
echo Creation du raccourci "Tunisie Telecom" sur le Bureau...

set "SCRIPT_DIR=%~dp0"
set "TARGET=%SCRIPT_DIR%LANCER-WINDOWS.bat"
set "ICON=%SCRIPT_DIR%tt-logo.ico"
set "SHORTCUT=%USERPROFILE%\Desktop\Tunisie Telecom.lnk"

powershell -NoProfile -Command ^
  "$s = (New-Object -COM WScript.Shell).CreateShortcut('%SHORTCUT%');" ^
  "$s.TargetPath = '%TARGET%';" ^
  "$s.WorkingDirectory = '%SCRIPT_DIR%';" ^
  "$s.IconLocation = '%ICON%';" ^
  "$s.WindowStyle = 1;" ^
  "$s.Description = 'Tunisie Telecom - Geolocalisation des sites';" ^
  "$s.Save()"

if exist "%SHORTCUT%" (
    echo.
    echo Raccourci cree avec succes sur le Bureau !
    echo Double-clique dessus pour lancer l'application.
) else (
    echo.
    echo [ERREUR] Le raccourci n'a pas pu etre cree.
)
echo.
pause
