#!/bin/bash
# 确认 MySQL 服务在运行（3306 可连）
# 用法: ./scripts/ensure-mysql.sh

source "$(dirname "$0")/common.sh"

HOST="${MYSQL_HOST:-127.0.0.1}"
PORT="${MYSQL_PORT:-3306}"

if python3 -c "import socket; s=socket.create_connection(('$HOST', int('$PORT')), 2); s.close()" 2>/dev/null; then
  echo "MySQL 端口 $PORT 已监听"
  exit 0
fi

echo "MySQL 未在 $HOST:$PORT 监听。"
if [[ -x /usr/local/mysql/support-files/mysql.server ]]; then
  echo "尝试启动（可能需要 sudo 密码）..."
  sudo /usr/local/mysql/support-files/mysql.server start || true
  sleep 2
  if python3 -c "import socket; s=socket.create_connection(('$HOST', int('$PORT')), 2); s.close()" 2>/dev/null; then
    echo "MySQL 已启动"
    exit 0
  fi
fi

echo "请手动启动 MySQL 后重试。"
exit 1
