#!/bin/bash
set -e
# 清洗 rounds 表 modern_place / geo_query
# 用法: ./scripts/clean-modern-places.sh

DIR="$(dirname "$0")"
source "$DIR/common.sh"
load_env

pip3 install pymysql -q 2>/dev/null || true
python3 "$DIR/clean-modern-places.py"
