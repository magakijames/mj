@echo off
rem Starts Snap and Pack: the website and the API together.
rem
rem   run.bat        uses Microsoft SQL Server (run the scripts in ..\database first)
rem   run.bat h2     uses a temporary in-memory database, no SQL Server needed
rem
rem Stop the server with Ctrl+C.
setlocal
cd /d "%~dp0"

set "JAR=target\snap-and-pack-backend-0.1.0.jar"
set "LOG=logs\server.log"

if exist "%JAR%" goto built
echo The backend has not been built yet. Building it now...
set "SNAP_NO_PAUSE=1"
call build.bat
if errorlevel 1 exit /b 1
:built

rem ---- Which database ----
set "MODE="
if /i "%~1"=="h2" set "MODE=--spring.profiles.active=h2"
if defined MODE goto h2note
if not defined DB_USERNAME set /p "DB_USERNAME=SQL Server login name, for example sa: "
if not defined DB_PASSWORD set /p "DB_PASSWORD=SQL Server password: "
goto pickport
:h2note
echo Using the temporary in-memory database. Data is erased when the server stops.

rem ---- Find a free port, starting at 8080 ----
:pickport
set "PORT=8080"
:tryport
if %PORT% GEQ 8090 goto noport
netstat -ano | findstr /r /c:":%PORT% .*LISTENING" >nul
if errorlevel 1 goto portok
echo Port %PORT% is already used by another program on this computer.
set /a PORT+=1
goto tryport
:noport
echo Ports 8080 to 8089 are all in use. Close other copies of this server and try again.
goto end
:portok
if not "%PORT%"=="8080" echo Using port %PORT% instead. If an older copy of this server is still open in another window, close it.

rem ---- Start ----
if exist "%LOG%" del "%LOG%"
echo.
echo Starting Snap and Pack...
echo When you see "Snap & Pack is running", open http://localhost:%PORT% in your browser.
echo.
java -jar "%JAR%" %MODE% --server.port=%PORT% "--logging.file.name=%LOG%"

rem ---- If it never got going, say why ----
echo.
if not exist "%LOG%" goto nolog
findstr /c:"Snap & Pack is running" "%LOG%" >nul 2>nul
if not errorlevel 1 goto end

echo The server could not start. What went wrong:
set "FOUND="
call :check "Login failed" "SQL Server did not accept that login name or password."
call :check "The TCP/IP connection to the host" "SQL Server is not reachable on localhost port 1433. Start SQL Server and enable TCP/IP for it in SQL Server Configuration Manager."
call :check "Cannot open database" "The SnapAndPack database is missing, or this login may not open it. Run database\01_create_database.sql."
call :check "Invalid object name" "The tables are missing. Run database\02_schema.sql with the SnapAndPack database selected."
call :check "was already in use" "The port was taken by another program just as the server started. Run run.bat again."
if not defined FOUND echo   - The cause is not one this script knows. Send the file backend\%LOG% to get it fixed.
echo.
echo The full details are in backend\%LOG%
echo To check that everything else works without SQL Server, run:  run.bat h2
goto end

:nolog
echo Java did not start. Check that JDK 17 or newer is installed: run  java -version

:end
echo.
echo The server has stopped.
pause
endlocal
exit /b 0

rem Prints the message when the log contains the text.
:check
findstr /c:%1 "%LOG%" >nul 2>nul
if errorlevel 1 exit /b 0
set "FOUND=1"
echo   - %~2
exit /b 0
