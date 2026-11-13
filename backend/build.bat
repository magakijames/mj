@echo off
rem Builds the Snap & Pack backend into target\snap-and-pack-backend-0.1.0.jar
rem Needs JDK 17 or newer. Maven is downloaded automatically the first time.
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 if not defined JAVA_HOME (
    echo Java was not found. Install JDK 17 or newer, then open a new Command Prompt and try again.
    echo Download: https://adoptium.net
    pause
    exit /b 1
)

echo Building the backend. The first build downloads its libraries and can take a few minutes...
call mvnw.cmd -q clean package -DskipTests
if errorlevel 1 (
    echo.
    echo BUILD FAILED. Read the messages above.
    pause
    exit /b 1
)

echo.
echo Build finished. Start the server with run.bat
if not defined SNAP_NO_PAUSE pause
endlocal
