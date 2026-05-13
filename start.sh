#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVER_DIR="$SCRIPT_DIR/OpenGalaxyServer-dev/OpenGalaxyServer-dev"
PROXY_SCRIPT="$SCRIPT_DIR/proxy-server.py"
BROWSER_DIR="$SCRIPT_DIR/FlashBrowser/FlashBrowser-linux-x64"
LOG_DIR="$SCRIPT_DIR/logs"

mkdir -p "$LOG_DIR"

STARTUP_LOG="$LOG_DIR/startup.log"
exec > >(tee -a "$STARTUP_LOG") 2>&1 || true

PID_FILE="$SCRIPT_DIR/.start_pids"
> "$PID_FILE"

cleanup() {
    echo ""
    echo "Shutting down..."
    if [ -f "$PID_FILE" ]; then
        while read -r pid; do
            kill "$pid" 2>/dev/null || true
        done < "$PID_FILE"
        rm -f "$PID_FILE"
    fi
    echo "All services stopped."
    exit 0
}
trap cleanup SIGINT SIGTERM

check_deps() {
    if ! command -v java &>/dev/null; then
        echo "ERROR: java not found. Install JDK 17+."
        exit 1
    fi
    if ! command -v python3 &>/dev/null; then
        echo "ERROR: python3 not found."
        exit 1
    fi
}

wait_for_port() {
    local port=$1
    local name=$2
    local timeout=$3
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if ss -tlnp "sport = :$port" 2>/dev/null | grep -q ":$port"; then
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
    done
    return 1
}

wait_for_backend_log() {
    local needle="Started Go2SuperApplication"
    local timeout=$1
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if grep -q "$needle" "$LOG_DIR/backend.log" 2>/dev/null; then
            return 0
        fi
        sleep 3
        elapsed=$((elapsed + 3))
    done
    return 1
}

start_mongo() {
    echo "[1/5] MongoDB..."
    if command -v mongod &>/dev/null; then
        if ! pgrep -x mongod >/dev/null; then
            echo "  Starting MongoDB..."
            mkdir -p "$SCRIPT_DIR/data/db"
            mongod --dbpath "$SCRIPT_DIR/data/db" --fork --logpath "$LOG_DIR/mongod.log" || \
            echo "  WARN: Could not auto-start MongoDB. Start it manually."
        else
            echo "  Already running."
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
      -Dspring-boot.run.jvmArguments="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED" > "$LOG_DIR/backend.log" 2>&1 &
    echo $! >> "$PID_FILE"
    cd "$SCRIPT_DIR"

    echo "  Building & starting... (this can take a minute)"
    if wait_for_backend_log 180; then
        echo "  Backend is ready."
    else
        echo "  ERROR: Backend failed to start. Check logs/backend.log"
        exit 1
    fi
}

start_proxy() {
    echo "[3/5] Proxy server (port 8080)..."
    python3 "$PROXY_SCRIPT" > "$LOG_DIR/proxy.log" 2>&1 &
    echo $! >> "$PID_FILE"

    if wait_for_port 8080 "Proxy" 15; then
        echo "  Proxy is ready."
    else
        echo "  ERROR: Proxy failed to start. Check logs/proxy.log"
        exit 1
    fi
}

start_browser() {
    echo "[4/5] FlashBrowser..."
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

kill_stale() {
    echo "  Cleaning up stale processes..."
    fuser -k 9090/tcp 2>/dev/null || true
    fuser -k 5050/tcp 2>/dev/null || true
    fuser -k 5051/tcp 2>/dev/null || true
    fuser -k 5050/udp 2>/dev/null || true
    fuser -k 5150/tcp 2>/dev/null || true
    sleep 1
    rm -f "$PID_FILE"
}

GAME_PROXY_SCRIPT="/home/Delk/WORKING FOLDER ROCK/game-proxy.py"

start_gameproxy() {
    echo "    Game socket proxy (port 5050→5051)..."
    if [ -f "$GAME_PROXY_SCRIPT" ]; then
        python3 "$GAME_PROXY_SCRIPT" 5050 127.0.0.1 5051 > "$LOG_DIR/gameproxy.log" 2>&1 &
        echo $! >> "$PID_FILE"
        sleep 1
        if timeout 2 bash -c "echo >/dev/tcp/127.0.0.1/5050" 2>/dev/null; then
            echo "  Game proxy is ready."
        else
            echo "  WARN: Game proxy may not be up."
        fi
    else
        echo "  Skipped (game-proxy.py not found)."
    fi
}

check_deps
kill_stale
start_mongo
start_backend
start_proxy
start_gameproxy
start_browser

echo ""
echo "[5/5] Verifying..."
sleep 2
if ss -tlnp "sport = :9090" | grep -q ":9090" && ss -tlnp "sport = :8080" | grep -q ":8080"; then
    ts=$(date '+%Y-%m-%d %H:%M:%S')
    for lf in "$LOG_DIR"/backend.log "$LOG_DIR"/proxy.log "$LOG_DIR"/gameproxy.log; do
        echo "[$ts] ALL SYSTEMS READY - GOOD TO GO!" >> "$lf"
    done
    echo ""
    echo "  ======================================="
    echo "    ALL SYSTEMS READY - GOOD TO GO!   "
    echo "  ======================================="
    echo "    Backend  -> http://localhost:9090"
    echo "    Proxy    -> http://localhost:8080"
    echo "    Game     -> TCP ports 5050 (proxy), 5051, 5150"
    echo "  ======================================="
    echo "  Press Ctrl+C to stop everything."
else
    echo "  One or more services may not be up. Check logs/ for details."
fi

wait
