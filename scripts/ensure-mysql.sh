#!/bin/bash
# 确认 MySQL 可连接（服务器 MySQL 通过 SSH 隧道访问）
# 用法: ./scripts/ensure-mysql.sh
set -e

source "$(dirname "$0")/common.sh"

HOST="${MYSQL_HOST:-127.0.0.1}"
PORT="${MYSQL_PORT:-3306}"

if python3 -c "import socket; s=socket.create_connection(('$HOST', int('$PORT')), 2); s.close()" 2>/dev/null; then
  echo "MySQL $HOST:$PORT 已连通"
  exit 0
fi

echo "MySQL 未在 $HOST:$PORT 连通。"
echo "  请确保 SSH 隧道在运行: ./scripts/tunnel-mysql.sh"
exit 1
