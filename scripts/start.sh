#!/bin/bash
set -e
# 同时启动后端 + 管理端
# 用法: ./scripts/start.sh

DIR="$(dirname "$0")"

"$DIR/start-backend.sh"
"$DIR/start-admin.sh"

echo ""
echo "全部就绪："
echo "  管理端  http://localhost:5173"
echo "  API     http://localhost:${PORT:-8787}/api/dynasties"
echo ""
echo "停止: ./scripts/stop.sh"
echo "日志: tail -f .logs/backend.log .logs/admin.log"
