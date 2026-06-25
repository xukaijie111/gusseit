#!/bin/bash
set -e
# 初始化 guseeit 数据库（执行 sql/schema.sql）
# 用法: ./scripts/init-mysql.sh

source "$(dirname "$0")/common.sh"
load_env

MYSQL_BIN="${MYSQL_BIN:-mysql}"
if [[ -x /usr/local/mysql/bin/mysql ]]; then
  MYSQL_BIN=/usr/local/mysql/bin/mysql
fi

HOST="${MYSQL_HOST:-127.0.0.1}"
PORT="${MYSQL_PORT:-3306}"
USER="${MYSQL_USER:-root}"
PASS="${MYSQL_PASSWORD:-}"
DB="${MYSQL_DATABASE:-guseeit}"
SCHEMA="$ROOT/sql/schema.sql"

if [[ -z "$PASS" ]]; then
  echo "MYSQL_PASSWORD 未配置。请在 .env 填写 root 密码，或留空以使用 H2（dev profile）。"
  exit 1
fi

if [[ ! -f "$SCHEMA" ]]; then
  echo "缺少 $SCHEMA"
  exit 1
fi

MYSQL_OPTS=(--connect-timeout=3 -h "$HOST" -P "$PORT" -u "$USER" -p"$PASS")

echo "连接 MySQL $USER@$HOST:$PORT ..."
if ! "$MYSQL_BIN" "${MYSQL_OPTS[@]}" -e "SELECT 1" >/dev/null 2>&1; then
  echo "MySQL 连接失败。请确认："
  echo "  1. MySQL 服务已启动（系统偏好设置 / mysql.server start）"
  echo "  2. .env 中 MYSQL_PASSWORD 与本地 root 密码一致"
  exit 1
fi

echo "执行建表脚本..."
"$MYSQL_BIN" "${MYSQL_OPTS[@]}" <"$SCHEMA"
echo "数据库 $DB 已就绪"
