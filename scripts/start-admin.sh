#!/bin/bash
set -e
# 启动 React 管理端（5173）
# 用法: ./scripts/start-admin.sh

source "$(dirname "$0")/common.sh"
ensure_log_dir

ADMIN_PORT=5173
LOG_FILE="$LOG_DIR/admin.log"

if lsof -ti :"$ADMIN_PORT" >/dev/null 2>&1; then
  echo "端口 $ADMIN_PORT 已被占用，请先执行 ./scripts/stop.sh"
  exit 1
fi

cd "$ROOT/admin"
if [[ ! -d node_modules ]]; then
  echo "安装前端依赖..."
  npm install
fi

echo "启动管理端 (port=$ADMIN_PORT)..."
echo "日志: $LOG_FILE"

nohup npm run dev -- --host 127.0.0.1 --port "$ADMIN_PORT" >>"$LOG_FILE" 2>&1 &
echo $! >"$LOG_DIR/admin.pid"

for i in $(seq 1 30); do
  if curl -sf "http://127.0.0.1:$ADMIN_PORT" >/dev/null 2>&1; then
    echo "管理端已就绪: http://localhost:$ADMIN_PORT"
    exit 0
  fi
  sleep 1
done

echo "管理端启动超时，请查看: tail -f $LOG_FILE"
exit 1
