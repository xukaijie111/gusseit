#!/bin/bash
set -e
# 重启 Java 后端（编译 jar + nohup 守护启动）
# 用法: ./scripts/restart.sh

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "停旧进程..."
bash "$ROOT/scripts/stop.sh"

echo "编译..."
cd "$ROOT/backend"
mvn -q package -DskipTests

echo "启动..."
bash "$ROOT/scripts/start-backend.sh"
