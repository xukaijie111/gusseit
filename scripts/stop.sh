#!/bin/bash
# 停止后端与管理端
# 用法: ./scripts/stop.sh

source "$(dirname "$0")/common.sh"
ensure_log_dir

stop_pid_file() {
  local name=$1
  local file=$2
  local port=$3

  if [[ -f "$file" ]]; then
    local pid
    pid=$(cat "$file")
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      echo "已停止 $name (pid $pid)"
    fi
    rm -f "$file"
  fi

  if [[ -n "$port" ]]; then
    local pids
    pids=$(lsof -ti :"$port" 2>/dev/null || true)
    if [[ -n "$pids" ]]; then
      echo "$pids" | xargs kill -9 2>/dev/null || true
      echo "已释放端口 $port"
    fi
  fi
}

stop_pid_file "backend" "$LOG_DIR/backend.pid" "${PORT:-8787}"
stop_pid_file "admin" "$LOG_DIR/admin.pid" "5173"

# 清理 maven/vite 子进程
pkill -f "guseeit-backend" 2>/dev/null || true
pkill -f "GuseeitApplication" 2>/dev/null || true

echo "完成"
