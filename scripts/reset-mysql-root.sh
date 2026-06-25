#!/bin/bash
# 将本地 MySQL root 密码重置为 .env 中的 MYSQL_PASSWORD（默认 123456）
# 需要 sudo，请在终端交互执行: ./scripts/reset-mysql-root.sh

set -e

source "$(dirname "$0")/common.sh"
load_env

NEW_PASS="${MYSQL_PASSWORD:-123456}"
MYSQL_HOME="${MYSQL_HOME:-/usr/local/mysql}"
SERVER="$MYSQL_HOME/support-files/mysql.server"

if [[ ! -x "$SERVER" ]]; then
  echo "未找到 $SERVER，请确认 MySQL 安装路径"
  exit 1
fi

echo "将把 root@localhost 密码设为: $NEW_PASS"
echo "需要输入 macOS 管理员密码 (sudo)"
read -r -p "继续? [y/N] " ans
[[ "$ans" =~ ^[Yy]$ ]] || exit 0

sudo "$SERVER" stop || true
sleep 2

# MySQL 8 跳过权限表启动
sudo "$MYSQL_HOME/bin/mysqld_safe" --skip-grant-tables --skip-networking &
SAFE_PID=$!
sleep 5

"$MYSQL_HOME/bin/mysql" -u root <<SQL
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '${NEW_PASS}';
FLUSH PRIVILEGES;
SQL

sudo kill "$SAFE_PID" 2>/dev/null || sudo pkill -f "mysqld_safe.*skip-grant" || true
sleep 2
sudo "$SERVER" start
sleep 3

"$MYSQL_HOME/bin/mysql" -u root -p"$NEW_PASS" -h127.0.0.1 -e "SELECT VERSION();"
echo "密码已重置。接下来执行: ./scripts/init-mysql.sh && ./scripts/start-backend.sh"
