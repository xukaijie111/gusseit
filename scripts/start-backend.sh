#!/bin/bash
set -e
# 启动 Java 后端（8787）
# 用法: ./scripts/start-backend.sh
#  MYSQL_PASSWORD 已配置 → MySQL；未配置 → dev（H2 内存库）

source "$(dirname "$0")/common.sh"
SCRIPT_DIR="$(dirname "$0")"
load_env
ensure_log_dir

PROFILE="$(backend_profile)"
LOG_FILE="$LOG_DIR/backend.log"

# 如果连的是 3307（隧道端口）且隧道没开，自动启动 SSH 隧道
if [[ "${MYSQL_PORT:-3306}" == "3307" ]]; then
  if ! lsof -i :3307 >/dev/null 2>&1; then
    ECS_ENV="$ROOT/deploy/ecs.env"
    if [[ -f "$ECS_ENV" ]]; then
      echo "建立 SSH 隧道: 127.0.0.1:3307 → 阿里云 MySQL..."
      source "$ECS_ENV"
      nohup sshpass -p "${ECS_PASSWORD}" ssh \
        -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
        -o ServerAliveInterval=30 -N -L 3307:127.0.0.1:3306 \
        "${ECS_USER}@${ECS_HOST}" >/dev/null 2>&1 &
      sleep 3
    fi
  fi
fi

if [[ "$PROFILE" == "default" ]]; then
  if ! "$SCRIPT_DIR/ensure-mysql.sh"; then
    exit 1
  fi
  if ! "$SCRIPT_DIR/init-mysql.sh"; then
    exit 1
  fi
fi

if lsof -ti :"${PORT:-8787}" >/dev/null 2>&1; then
  echo "端口 ${PORT:-8787} 已被占用，请先执行 ./scripts/stop.sh"
  exit 1
fi

echo "启动后端 (profile=$PROFILE, port=${PORT:-8787})..."
echo "日志: $LOG_FILE"

cd "$ROOT/backend"
# 先构建 jar（如果不存在）
JAR=$(ls target/*.jar 2>/dev/null | head -1)
if [[ -z "$JAR" ]]; then
  echo "构建 jar..."
  mvn -q package -DskipTests || exit 1
  JAR=$(ls target/*.jar 2>/dev/null | head -1)
fi

nohup env \
  MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}" \
  MYSQL_PORT="${MYSQL_PORT:-3306}" \
  MYSQL_USER="${MYSQL_USER:-root}" \
  MYSQL_PASSWORD="${MYSQL_PASSWORD:-}" \
  MYSQL_DATABASE="${MYSQL_DATABASE:-guseeit}" \
  AMAP_WEB_KEY="${AMAP_WEB_KEY:-}" \
  AMAP_BASE_URL="${AMAP_BASE_URL:-https://restapi.amap.com/v3}" \
  DOUYIN_APP_ID="${DOUYIN_APP_ID:-}" \
  DOUYIN_APP_SECRET="${DOUYIN_APP_SECRET:-}" \
  PORT="${PORT:-8787}" \
  HOST="${HOST:-0.0.0.0}" \
  java -jar "$JAR" >>"$LOG_FILE" 2>&1 < /dev/null &
BACKEND_PID=$!
echo $BACKEND_PID >"$LOG_DIR/backend.pid"
disown $BACKEND_PID 2>/dev/null || true

for i in $(seq 1 60); do
  if curl -sf "http://127.0.0.1:${PORT:-8787}/api/dynasties" >/dev/null 2>&1; then
    echo "后端已就绪: http://localhost:${PORT:-8787}"
    exit 0
  fi
  sleep 1
done

echo "后端启动超时，请查看: tail -f $LOG_FILE"
exit 1
