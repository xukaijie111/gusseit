#!/bin/bash
# 公共：项目根目录、加载 .env

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT/.env"
LOG_DIR="$ROOT/.logs"

load_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    echo "缺少 $ENV_FILE，请复制 .env.example 并填写配置"
    exit 1
  fi
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
}

backend_profile() {
  if [[ -n "${MYSQL_PASSWORD:-}" ]]; then
    echo "default"
  else
    echo "dev"
  fi
}

ensure_log_dir() {
  mkdir -p "$LOG_DIR"
}
