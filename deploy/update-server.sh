#!/bin/bash
# 增量更新：更新 jar、admin dist、Nginx 配置，重启服务
set -euo pipefail

# 此脚本通过 SSH 在 ECS 上执行
REMOTE=1

log() { echo "[$(date '+%H:%M:%S')] $*"; }

if [ -z "${REMOTE:-}" ]; then
    log "请通过 deploy-aliyun.sh 调用本脚本，不要直接执行"
    exit 1
fi

APP_ROOT="${ECS_REMOTE_DIR:-/opt/guseeit}"

log "=== Guseeit 增量更新 ==="

# 1. 确保 Java 可用
if ! command -v java >/dev/null 2>&1; then
    log "Java 未安装，请先执行 setup-server.sh"
    exit 1
fi

# 2. 确保 Nginx 配置最新
log "更新 Nginx 配置"
cp "${APP_ROOT}/deploy/nginx-guseeit.conf" /etc/nginx/conf.d/guseeit.conf
nginx -t
systemctl reload nginx

# 3. 重启后端
log "重启后端服务"
systemctl daemon-reload
systemctl restart guseeit-api
sleep 6
systemctl status guseeit-api --no-pager --lines=5

# 4. 校验环境变量已注入（不打印密钥）
if grep -q '^OSS_IMAGE_DISPLAY_WIDTH=' "${APP_ROOT}/.env" 2>/dev/null; then
  log "OSS 展示图: $(grep '^OSS_IMAGE_DISPLAY_WIDTH=' "${APP_ROOT}/.env" | cut -d= -f2)px"
fi

log "=== Guseeit 更新完成 ==="
