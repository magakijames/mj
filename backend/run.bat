@echo off
rem Starts the Snap & Pack backend on http://localhost:8080
rem
rem   run.bat        uses Microsoft SQL Server (run the scripts in ..\database first)
rem   run.bat h2     uses a temporary in-memory database, no SQL Server needed
rem
rem Stop the server with Ctrl+C.
setlocal
cd /d "%~dp0"

set "JAR=target\snap-and-pack-backend-0.1.0.jar"

if not exist "%JAR%" (
    echo The backend has not been built yet. Building it now...
    set "SNAP_NO_PAUSE=1"
    call build.bat
    if errorlevel 1 exit /b 1
)

if /i "%~1"=="h2" (
    echo Starting with the temporary in-memory database. Data is erased when the server stops.
    java -jar "%JAR%" --spring.profiles.active=h2
    goto :end
)

rem Ask for the SQL Server login unless it is already set as environment variables.
if not defined DB_USERNAME set /p "DB_USERNAME=SQL Server login name (for example sa): "
if not defined DB_PASSWORD set /p "DB_PASSWORD=SQL Server password: "

echo Starting the backend on http://localhost:8080 ...
java -jar "%JAR%"

:end
echo.
echo The server has stopped.
pause
endlocal
