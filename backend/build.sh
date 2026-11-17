#!/usr/bin/env bash
# Builds the Snap & Pack backend into target/snap-and-pack-backend-0.1.0.jar
# Needs JDK 21 or newer. Maven is downloaded automatically the first time.
#
#   ./build.sh            (or:  bash build.sh)

set -u
cd "$(dirname "$0")"

# ---- Is Java 21 or newer installed? ----
if ! command -v java >/dev/null 2>&1; then
    echo "Java was not found. Install JDK 21 or newer, for example:"
    echo "    sudo apt update && sudo apt install openjdk-21-jdk"
    exit 1
fi
JAVA_MAJOR=$(java -version 2>&1 | grep -m1 -oE 'version "[0-9]+' | grep -oE '[0-9]+$')
if [ -n "$JAVA_MAJOR" ] && [ "$JAVA_MAJOR" -lt 21 ]; then
    echo "Java $JAVA_MAJOR is installed, but this project needs Java 21 or newer, for example:"
    echo "    sudo apt update && sudo apt install openjdk-21-jdk"
    echo "If several versions are installed, choose one with:  sudo update-alternatives --config java"
    exit 1
fi

echo "Building the backend. The first build downloads its libraries and can take a few minutes..."
# Run the Maven wrapper through sh, so it works even if the file lost its "executable" mark when unzipped.
if ! sh ./mvnw -q clean package -DskipTests; then
    echo
    echo "BUILD FAILED. Read the messages above."
    exit 1
fi

echo
echo "Build finished. Start the server with ./run.sh"
