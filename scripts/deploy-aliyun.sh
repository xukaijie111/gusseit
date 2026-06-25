#!/bin/bash
# 本地一键部署到阿里云 ECS
# 用法:
#   bash deploy/aliyun-deploy.sh              # 增量更新（首次也会自动初始化）
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/.."
DEPLOY_DIR="${PROJECT_DIR}/deploy"

source "${DEPLOY_DIR}/ecs.env"

log() { echo -e "\e[1;34m[$(date '+%H:%M:%S')]\e[0m $*"; }

# SSH 命令别名
SSH_CMD="sshpass -p '${ECS_PASSWORD}' ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p ${ECS_PORT} ${ECS_USER}@${ECS_HOST}"
SCP_CMD="sshpass -p '${ECS_PASSWORD}' scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P ${ECS_PORT}"

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
log "jar: ${JAR_FILE}"

# 2. 构建 admin dist
log "构建 admin"
cd "${PROJECT_DIR}/admin"
npm run build 2>&1 | tail -1

# 3. 创建远程目录
log "创建远程目录"
${SSH_CMD} "mkdir -p ${ECS_REMOTE_DIR}/admin/dist ${ECS_REMOTE_DIR}/deploy ${ECS_REMOTE_DIR}/scripts"

# 4. 上传 jar + admin dist + deploy 脚本 + .env
log "上传文件"
${SCP_CMD} "${JAR_FILE}"              "${ECS_USER}@${ECS_HOST}:${ECS_REMOTE_DIR}/guseeit-backend.jar"
# 打包 admin dist 并上传
tar czf /tmp/admin-dist.tar.gz -C "${PROJECT_DIR}/admin/dist" .
${SCP_CMD} /tmp/admin-dist.tar.gz "${ECS_USER}@${ECS_HOST}:/tmp/admin-dist.tar.gz"
${SSH_CMD} "rm -rf ${ECS_REMOTE_DIR}/admin/dist/* && tar xzf /tmp/admin-dist.tar.gz -C ${ECS_REMOTE_DIR}/admin/dist/ && rm /tmp/admin-dist.tar.gz"
${SCP_CMD} "${PROJECT_DIR}/.env"      "${ECS_USER}@${ECS_HOST}:${ECS_REMOTE_DIR}/.env"
${SCP_CMD} -r "${DEPLOY_DIR}/"*       "${ECS_USER}@${ECS_HOST}:${ECS_REMOTE_DIR}/deploy/"

# 5. 执行远程初始化/更新
REMOTE_SETUP="${ECS_REMOTE_DIR}/deploy/setup-server.sh"
REMOTE_UPDATE="${ECS_REMOTE_DIR}/deploy/update-server.sh"

# 判断是否首次部署
HAS_SERVICE=$(${SSH_CMD} "systemctl is-enabled guseeit-api 2>/dev/null || true")
if [ -z "$HAS_SERVICE" ] || [ "$HAS_SERVICE" = "disabled" ]; then
    log "首次部署，执行 setup-server.sh"
    ${SSH_CMD} "bash ${REMOTE_SETUP}"
else
    log "增量更新，执行 update-server.sh"
    ${SSH_CMD} "bash ${REMOTE_UPDATE}"
fi

log "=== 部署完成 ==="
log "API:     https://api.agentnow.fun"
log "Admin:   https://admin.agentnow.fun"
log ""
log "⚠️  请确保 DNS 已配置两条记录:"
log "   api.agentnow.fun   → ${ECS_HOST}"
log "   admin.agentnow.fun → ${ECS_HOST}"
