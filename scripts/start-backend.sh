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
nohup mvn -q spring-boot:run -Dspring-boot.run.profiles="$PROFILE" >>"$LOG_FILE" 2>&1 &
echo $! >"$LOG_DIR/backend.pid"

for i in $(seq 1 60); do
  if curl -sf "http://127.0.0.1:${PORT:-8787}/api/dynasties" >/dev/null 2>&1; then
    echo "后端已就绪: http://localhost:${PORT:-8787}"
    exit 0
  fi
  sleep 1
done

echo "后端启动超时，请查看: tail -f $LOG_FILE"
exit 1
