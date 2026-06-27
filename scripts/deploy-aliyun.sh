#!/bin/bash
# 本地一键部署到阿里云 ECS
# 用法: bash scripts/deploy-aliyun.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/.."
DEPLOY_DIR="${PROJECT_DIR}/deploy"

source "${DEPLOY_DIR}/ecs.env"

log() { echo -e "\e[1;34m[$(date '+%H:%M:%S')]\e[0m $*"; }

ssh_cmd() {
  sshpass -p "${ECS_PASSWORD}" ssh \
    -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    -p "${ECS_PORT}" \
    "${ECS_USER}@${ECS_HOST}" "$@"
}

scp_cmd() {
  sshpass -p "${ECS_PASSWORD}" scp \
    -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    -P "${ECS_PORT}" "$@"
}

log "=== 开始部署 Guseeit 到 ${ECS_HOST} ==="

# 1. 构建后端 jar
log "构建后端 jar"
cd "${PROJECT_DIR}/backend"
mvn package -DskipTests -q
JAR_FILE=$(ls target/guseeit-backend-*.jar 2>/dev/null | head -1)
if [ -z "$JAR_FILE" ]; then
    log "❌ 未找到 jar 文件"
    exit 1
fi
JAR_FILE="${PROJECT_DIR}/backend/${JAR_FILE}"
log "jar: ${JAR_FILE}"

# 2. 构建 admin dist
log "构建 admin"
cd "${PROJECT_DIR}/admin"
npm run build 2>&1 | tail -1

# 3. 创建远程目录
log "创建远程目录"
ssh_cmd "mkdir -p ${ECS_REMOTE_DIR}/admin/dist ${ECS_REMOTE_DIR}/deploy ${ECS_REMOTE_DIR}/scripts"

# 4. 上传 jar + admin dist + deploy 脚本 + .env（ECS 上 MySQL 用 3306，非本地隧道 3307）
log "上传文件"
sed \
  -e 's/^MYSQL_PORT=3307/MYSQL_PORT=3306/' \
  -e 's/^MYSQL_HOST=127.0.0.1/MYSQL_HOST=127.0.0.1/' \
  "${PROJECT_DIR}/.env" > /tmp/guseeit-server.env
grep -q '^MYSQL_PORT=3306' /tmp/guseeit-server.env || echo 'MYSQL_PORT=3306' >> /tmp/guseeit-server.env
scp_cmd "${JAR_FILE}" "${ECS_USER}@${ECS_HOST}:${ECS_REMOTE_DIR}/guseeit-backend.jar"
tar czf /tmp/admin-dist.tar.gz -C "${PROJECT_DIR}/admin/dist" .
scp_cmd /tmp/admin-dist.tar.gz "${ECS_USER}@${ECS_HOST}:/tmp/admin-dist.tar.gz"
ssh_cmd "rm -rf ${ECS_REMOTE_DIR}/admin/dist/* && tar xzf /tmp/admin-dist.tar.gz -C ${ECS_REMOTE_DIR}/admin/dist/ && rm /tmp/admin-dist.tar.gz"
scp_cmd /tmp/guseeit-server.env "${ECS_USER}@${ECS_HOST}:${ECS_REMOTE_DIR}/.env"
scp_cmd -r "${DEPLOY_DIR}/"* "${ECS_USER}@${ECS_HOST}:${ECS_REMOTE_DIR}/deploy/"

# 5. 执行远程初始化/更新
REMOTE_SETUP="${ECS_REMOTE_DIR}/deploy/setup-server.sh"
REMOTE_UPDATE="${ECS_REMOTE_DIR}/deploy/update-server.sh"

HAS_SERVICE=$(ssh_cmd "systemctl is-enabled guseeit-api 2>/dev/null || true")
if [ -z "$HAS_SERVICE" ] || [ "$HAS_SERVICE" = "disabled" ]; then
    log "首次部署，执行 setup-server.sh"
    ssh_cmd "bash ${REMOTE_SETUP}"
else
    log "增量更新，执行 update-server.sh"
    ssh_cmd "bash ${REMOTE_UPDATE}"
fi

# 6. 验证 API 与环境变量
log "验证部署"
sleep 5
HTTP_CODE="000"
HTTP_CODE="$(curl -sS -o /tmp/guseeit-deploy-check.json -w '%{http_code}' 'https://api.agentnow.fun/api/game/dynasties' 2>/dev/null || echo 000)"
if [ "$HTTP_CODE" = "200" ]; then
  log "✅ API 正常 (HTTP $HTTP_CODE)"
else
  log "⚠️  API 返回 HTTP $HTTP_CODE，请检查 journalctl -u guseeit-api"
fi
ssh_cmd "grep -E '^(OSS_IMAGE_DISPLAY|MYSQL_PORT|OSS_PUBLIC)=' ${ECS_REMOTE_DIR}/.env | sed 's/=.*/=***/' || true"

log "=== 部署完成 ==="
log "API:     https://api.agentnow.fun"
log "Admin:   https://admin.agentnow.fun"
log ""
log "⚠️  请确保 DNS 已配置两条记录:"
log "   api.agentnow.fun   → ${ECS_HOST}"
log "   admin.agentnow.fun → ${ECS_HOST}"
