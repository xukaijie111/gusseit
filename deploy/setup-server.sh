#!/bin/bash
# 首次部署：安装 Java、初始化 DB、配置 Nginx + systemd
set -euo pipefail

# 从 ecs.env 加载变量
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/.."
APP_ROOT="${ECS_REMOTE_DIR:-/opt/guseeit}"

source "${PROJECT_DIR}/deploy/ecs.env"

# 此脚本通过 SSH 在 ECS 上执行
REMOTE=1

log() { echo "[$(date '+%H:%M:%S')] $*"; }

if [ -z "${REMOTE:-}" ]; then
    log "请通过 deploy-aliyun.sh 调用本脚本，不要直接执行"
    exit 1
fi

log "=== 开始 Guseeit 初始化 ==="

# 1. 安装 Java 8 / 11 (Corretto)
log "检查 Java 运行时"
if ! command -v java >/dev/null 2>&1; then
    log "安装 Alibaba Dragonwell Java 11"
    dnf install -y java-11-alibaba-dragonwell
fi
java -version

# 2. 确保 MySQL 运行
log "启动 MySQL"
systemctl enable --now mysqld

# 3. 创建 guseeit 数据库
log "初始化 guseeit 数据库"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-xukaijie311700}"
mysql -uroot -p"${MYSQL_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS guseeit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
FLUSH PRIVILEGES;
SQL

# 4. 确保 Nginx 运行
log "启动 Nginx"
systemctl enable --now nginx

# 5. 部署 Nginx 配置
log "部署 Nginx 配置"
cp "${APP_ROOT}/deploy/nginx-guseeit.conf" /etc/nginx/conf.d/guseeit.conf
nginx -t
systemctl reload nginx

# 6. 注册 systemd 服务
log "注册 systemd 服务"
cp "${APP_ROOT}/deploy/guseeit-api.service" /etc/systemd/system/guseeit-api.service
systemctl daemon-reload
systemctl enable guseeit-api

# 7. 启动后端服务
log "启动后端服务"
systemctl restart guseeit-api
systemctl status guseeit-api --no-pager

log "=== Guseeit 初始化完成 ==="
