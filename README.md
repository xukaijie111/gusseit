
# Guseeit

中国历史时空猜谜 — 出题与题库管理。

## 结构

| 目录 | 说明 |
|------|------|
| `backend/` | Java Spring Boot API（8787） |
| `admin/` | React 管理端（5173） |
| `miniprogram/` | 抖音小程序（千年图谜） |
| `sql/schema.sql` | MySQL 建表脚本 |
| `scripts/` | 启动/停止脚本 |

## 配置

复制并编辑根目录 `.env`（参考 `.env.example`）：

- 必填：`OPENAI_*`、`ARK_*`、`OSS_*`、`AMAP_WEB_KEY`（地图选点逆地理）
- MySQL：`MYSQL_PASSWORD` 等（见下方说明）

## 一键启动

```bash
chmod +x scripts/*.sh

# 同时启动后端 + 管理端
./scripts/start.sh

# 停止
./scripts/stop.sh
```

| 脚本 | 作用 |
|------|------|
| `./scripts/start.sh` | 后端 + 前端 |
| `./scripts/start-backend.sh` | 仅 Java 后端 |
| `./scripts/start-admin.sh` | 仅 React 管理端 |
| `./scripts/stop.sh` | 停止全部 |

- 管理端：http://localhost:5173  
- API：http://localhost:8787/api/dynasties  
- 日志：`.logs/backend.log`、`.logs/admin.log`

## 数据库

| 情况 | 行为 |
|------|------|
| `.env` 里 **未填** `MYSQL_PASSWORD` | 自动用 `dev` profile（H2 内存库，无需建表） |
| **已填** `MYSQL_PASSWORD` | 使用 MySQL，需先建表：`mysql -u root -p < sql/schema.sql` |

## 手动启动（可选）

```bash
# 后端
set -a && source .env && set +a
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 前端
cd admin && npm install && npm run dev
```

## API

- `GET /api/dynasties` — 朝代列表
- `GET /api/rounds` — 题库
- `GET /api/jobs` / `GET /api/jobs/{id}` — 任务
- `POST /api/generate` — `{ "dynasty": "唐", "count": 5 }`

## 抖音小程序

AppID：`ttb252c4ef2c36d82607`

1. 用 [抖音开发者工具](https://developer.open-douyin.com/) 导入 `miniprogram/` 目录
2. 修改 `miniprogram/config.js` 里的 `apiBase`（真机调试填电脑局域网 IP，如 `http://192.168.x.x:8787`）
3. 抖音开放平台 → 开发管理 → 开发设置 → **服务器域名**，添加 API 的 HTTPS 域名（上线必须）
4. 先在管理端生成带图片的题目，再在小程序里「开始挑战」

### 小游戏 API

- `GET /api/game/dynasties` — 可选朝代
- `GET /api/game/session?dynasty=唐&count=5` — 拉取一局题目（仅图片，不含答案）
- `POST /api/game/guess` — `{ roundId, yearAd, latitude, longitude }` → 得分与揭晓
