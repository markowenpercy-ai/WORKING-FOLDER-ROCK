#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVER_DIR="$SCRIPT_DIR/OpenGalaxyServer-dev/OpenGalaxyServer-dev"
FLASK_DIR="$SCRIPT_DIR/../OpenGalaxyClient-prd"
LOG_DIR="$SCRIPT_DIR/logs"
BROWSER_DIR="$SCRIPT_DIR/FlashBrowser/FlashBrowser-linux-x64"

mkdir -p "$LOG_DIR"

PID_FILE="$SCRIPT_DIR/.start_pids"
MONGO_PID=""

cleanup() {
    echo ""
    echo "Shutting down all services..."

    # Kill all tracked PIDs and their children
    if [ -f "$PID_FILE" ]; then
        while read -r pid; do
            [ -n "$pid" ] && kill -TERM -- -$(ps -o pgid= -p "$pid" 2>/dev/null | tr -d ' ') 2>/dev/null || true
        done < "$PID_FILE"
        sleep 1
        # Force kill any survivors on our ports
        fuser -k 9090/tcp 2>/dev/null || true
        fuser -k 8080/tcp 2>/dev/null || true
        fuser -k 5050/tcp 2>/dev/null || true
        fuser -k 5051/tcp 2>/dev/null || true
        fuser -k 5150/tcp 2>/dev/null || true
        rm -f "$PID_FILE"
    fi

    # Stop MongoDB if we started it
    if [ -n "$MONGO_PID" ]; then
        echo "  Stopping MongoDB..."
        mongod --dbpath "$SCRIPT_DIR/data/db" --shutdown 2>/dev/null || kill "$MONGO_PID" 2>/dev/null || true
    fi

    echo "All services stopped."
    exit 0
}
trap cleanup SIGINT SIGTERM EXIT

check_deps() {
    if ! command -v java &>/dev/null; then echo "ERROR: java not found"; exit 1; fi
    if ! command -v python3 &>/dev/null; then echo "ERROR: python3 not found"; exit 1; fi
}

kill_stale() {
    echo "  Cleaning up stale processes..."
    fuser -k 9090/tcp 2>/dev/null || true
    fuser -k 8080/tcp 2>/dev/null || true
    fuser -k 5050/tcp 2>/dev/null || true
    fuser -k 5051/tcp 2>/dev/null || true
    fuser -k 5150/tcp 2>/dev/null || true
    sleep 1
}

start_mongo() {
    echo "[1/5] MongoDB..."
    if command -v mongod &>/dev/null; then
        if ! pgrep -x mongod >/dev/null; then
            echo "  Starting MongoDB..."
            mkdir -p "$SCRIPT_DIR/data/db"
            mongod --dbpath "$SCRIPT_DIR/data/db" --fork --logpath "$LOG_DIR/mongod.log" 2>/dev/null
            MONGO_PID=$(pgrep -x mongod | head -1)
            echo "  MongoDB started (PID $MONGO_PID)."
        else
            MONGO_PID=$(pgrep -x mongod | head -1)
            echo "  Already running (PID $MONGO_PID)."
        fi
    else
        echo "  WARN: mongod not found. Ensure MongoDB is running."
    fi
}

start_backend() {
    echo "[2/5] Backend server (port 9090)..."
    cd "$SERVER_DIR"
    mvn spring-boot:run -q \
      -Dspring-boot.run.profiles=dev \
      -Dspring-boot.run.jvmArguments="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED" \
      > "$LOG_DIR/backend.log" 2>&1 &
    BACKEND_PID=$!
    echo "$BACKEND_PID" >> "$PID_FILE"
    cd "$SCRIPT_DIR"

    echo "  Building & starting... (this can take a minute)"
    local elapsed=0
    while [ $elapsed -lt 180 ]; do
        if grep -q "Started Go2SuperApplication" "$LOG_DIR/backend.log" 2>/dev/null; then
            echo "  Backend is ready."
            return 0
        fi
        sleep 3
        elapsed=$((elapsed + 3))
    done
    echo "  ERROR: Backend failed to start. Check logs/backend.log"
    exit 1
}

start_gameproxy() {
    echo "[3/5] Game socket proxy (port 5050→5051)..."
    if [ -f "$SCRIPT_DIR/../game-proxy.py" ]; then
        python3 "$SCRIPT_DIR/../game-proxy.py" 5050 127.0.0.1 5051 > "$LOG_DIR/gameproxy.log" 2>&1 &
    elif [ -f "$SCRIPT_DIR/game-proxy.py" ]; then
        python3 "$SCRIPT_DIR/game-proxy.py" 5050 127.0.0.1 5051 > "$LOG_DIR/gameproxy.log" 2>&1 &
    else
        echo "  WARN: game-proxy.py not found."
        return
    fi
    echo $! >> "$PID_FILE"
    sleep 2
    echo "  Game proxy ready."
}

start_flask() {
    echo "[4/5] Flask client (port 8080)..."
    if [ -f "$FLASK_DIR/main.py" ]; then
        cd "$FLASK_DIR"
        FLASK_DEBUG=0 venv/bin/python main.py > "$LOG_DIR/flask.log" 2>&1 &
        echo $! >> "$PID_FILE"
        cd "$SCRIPT_DIR"
        sleep 3
        echo "  Flask client ready."
    else
        echo "  WARN: Flask main.py not found at $FLASK_DIR."
    fi
}

start_browser() {
    echo "[5/5] FlashBrowser..."
    if [ -f "$BROWSER_DIR/FlashBrowser" ]; then
        "$BROWSER_DIR/FlashBrowser" --no-sandbox "http://localhost:8080" > "$LOG_DIR/browser.log" 2>&1 &
        echo $! >> "$PID_FILE"
        echo "  Launched."
    else
        echo "  Skipped (not found at $BROWSER_DIR)."
    fi
}

echo ""
echo "  OpenGalaxy Auto-Start"
echo "  ---------------------"
echo ""

check_deps
kill_stale
start_mongo
start_backend
start_gameproxy
start_flask
start_browser

echo ""
echo "  ======================================="
echo "    ALL SYSTEMS READY - GOOD TO GO!   "
echo "  ======================================="
echo "    Backend      -> http://localhost:9090"
echo "    Flask Client -> http://localhost:8080"
echo "    Admin Panel  -> http://localhost:8080/admin.html"
echo "    Game         -> TCP ports 5050/5051/5150"
echo "  ======================================="
echo "  Press Ctrl+C to stop everything."
echo ""

wait
