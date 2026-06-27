#!/bin/bash
# 检查后端 / MySQL 隧道状态
# 用法: ./scripts/status.sh

source "$(dirname "$0")/common.sh"
load_env

PORT="${PORT:-8787}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

echo "=== Guseeit 服务状态 ==="

if lsof -i :"$PORT" >/dev/null 2>&1; then
  PID=$(lsof -ti :"$PORT" | head -1)
  echo "✅ 后端: 运行中 (port $PORT, pid $PID)"
  curl -sf -m 3 "http://127.0.0.1:$PORT/api/game/dynasties" >/dev/null \
    && echo "   API: /api/game/dynasties 正常" \
    || echo "   ⚠ API 无响应"
else
  echo "❌ 后端: 未运行 (port $PORT)"
  echo "   启动: ./scripts/start-backend.sh"
fi

if lsof -i :"$MYSQL_PORT" >/dev/null 2>&1; then
  echo "✅ MySQL: port $MYSQL_PORT 已监听"
else
  echo "❌ MySQL: port $MYSQL_PORT 未连通"
  if [[ "$MYSQL_PORT" == "3307" ]]; then
    echo "   请先运行: ./scripts/tunnel-mysql.sh"
  fi
fi

echo ""
echo "小程序 apiBase: miniprogram/config.js"
echo "  模拟器 → http://127.0.0.1:$PORT"
echo "  真机   → http://<电脑局域网IP>:$PORT"
