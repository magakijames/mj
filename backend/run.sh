#!/usr/bin/env bash
# Starts Snap & Pack: the website and the API together.
#
#   ./run.sh        uses Microsoft SQL Server (run the scripts in ../database first)
#   ./run.sh h2     uses a temporary in-memory database, no SQL Server needed
#
# The SQL Server address defaults to localhost:1433. If SQL Server runs on another computer,
# set DB_URL first, for example:
#   export DB_URL="jdbc:sqlserver://192.168.1.20:1433;databaseName=SnapAndPack;encrypt=true;trustServerCertificate=true"
#
# Stop the server with Ctrl+C.

set -u
cd "$(dirname "$0")"

JAR="target/snap-and-pack-backend-0.1.0.jar"
LOG="logs/server.log"

# ---- Build first if needed ----
if [ ! -f "$JAR" ]; then
    echo "The backend has not been built yet. Building it now..."
    bash ./build.sh || exit 1
fi

# ---- Which database ----
MODE=""
if [ "${1:-}" = "h2" ]; then
    MODE="--spring.profiles.active=h2"
    echo "Using the temporary in-memory database. Data is erased when the server stops."
else
    if [ -z "${DB_USERNAME:-}" ]; then
        read -r -p "SQL Server login name, for example sa: " DB_USERNAME
    fi
    if [ -z "${DB_PASSWORD:-}" ]; then
        # -s keeps the password off the screen while it is typed.
        read -r -s -p "SQL Server password: " DB_PASSWORD
        echo
    fi
    export DB_USERNAME DB_PASSWORD
fi

# ---- Is a port already in use? ----
port_in_use() {
    if command -v ss >/dev/null 2>&1; then
        ss -ltn "sport = :$1" 2>/dev/null | grep -q LISTEN
    else
        (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null
    fi
}

# ---- Find a free port, starting at 8080 ----
PORT=8080
while port_in_use "$PORT"; do
    echo "Port $PORT is already used by another program on this computer."
    PORT=$((PORT + 1))
    if [ "$PORT" -ge 8090 ]; then
        echo "Ports 8080 to 8089 are all in use. Close other copies of this server and try again."
        echo "To see what is using port 8080:  sudo ss -ltnp 'sport = :8080'"
        exit 1
    fi
done
if [ "$PORT" != "8080" ]; then
    echo "Using port $PORT instead. If an older copy of this server is still running in another terminal, close it."
fi

# ---- Start ----
mkdir -p logs
rm -f "$LOG"
echo
echo "Starting Snap & Pack..."
echo "When you see \"Snap & Pack is running\", open http://localhost:$PORT in your browser."
echo
# $MODE is left unquoted on purpose: when it is empty it must add nothing.
java -jar "$JAR" $MODE --server.port="$PORT" --logging.file.name="$LOG"

# ---- If it never got going, say why ----
echo
if [ ! -f "$LOG" ]; then
    echo "Java did not start. Check that JDK 21 or newer is installed:  java -version"
    echo "The server has stopped."
    exit 1
fi
if grep -q "Snap & Pack is running" "$LOG"; then
    echo "The server has stopped."
    exit 0
fi

echo "The server could not start. What went wrong:"
FOUND=""
# Prints the advice when the log contains the text.
check() {
    if grep -q "$1" "$LOG"; then
        echo "  - $2"
        FOUND=1
    fi
}
check "Login failed" "SQL Server did not accept that login name or password."
check "The TCP/IP connection to the host" "SQL Server is not reachable at the address in DB_URL (localhost:1433 unless you set it). Check that SQL Server is running and that the port is open."
check "Cannot open database" "The SnapAndPack database is missing, or this login may not open it. Run database/01_create_database.sql."
check "Invalid object name" "A table is missing. As an administrator run, in order, any of the scripts in the database folder you have not run yet."
check "Invalid column name" "The database is missing the newest columns. As an administrator run, in number order, the scripts in the database folder you have not run yet (the newest is 06)."
check "permission was denied" "The database login lacks a permission it needs. Run database/04_app_login_permissions.sql again as an administrator."
check "was already in use" "The port was taken by another program just as the server started. Run ./run.sh again."
if [ -z "$FOUND" ]; then
    echo "  - The cause is not one this script knows. Send the file backend/$LOG to get it fixed."
fi
echo
echo "The full details are in backend/$LOG"
echo "To check that everything else works without SQL Server, run:  ./run.sh h2"
echo "The server has stopped."
exit 1
