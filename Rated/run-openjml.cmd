@echo off
setlocal

title OpenJML ESC e RAC - Rated
set "WSL_DISTRO=Ubuntu-24.04"

pushd "%~dp0"
wsl.exe -d %WSL_DISTRO% --cd "%CD%" -- bash ./run-openjml.sh
set "OPENJML_EXIT=%ERRORLEVEL%"
popd

:finished
echo.
echo Premi un tasto per chiudere questa finestra.
pause >nul
exit /b %OPENJML_EXIT%
