#!/bin/bash
# 建立 SSH 隧道，把阿里云 MySQL 映射到本地 3307
# 用法: bash scripts/tunnel-mysql.sh
set -e

# 从 deploy/ecs.env 加载连接信息
source "$(cd "$(dirname "$0")" && pwd)/../deploy/ecs.env"

echo "建立 SSH 隧道: 本地 127.0.0.1:3307 → ${ECS_HOST}:3306"
echo "按 Ctrl+C 断开隧道"
echo ""

sshpass -p "${ECS_PASSWORD}" ssh \
    -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    -o ServerAliveInterval=60 \
    -N \
    -L 3307:127.0.0.1:3306 \
    "${ECS_USER}@${ECS_HOST}"
