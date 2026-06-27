"""
批量调用 Qwen 扩充 anecdote_data 的 summary 到 200-300 字。
用法: python3 scripts/expand_summaries.py [起始id] [数量]
默认: 全量处理所有 summary < 80 字的记录。
"""
import pymysql, sys, time, json, requests

# 加载 .env 配置
env = {}
with open(".env") as f:
    for line in f:
        line = line.strip()
        if line and "=" in line and not line.startswith("#"):
            k, v = line.split("=", 1)
            env[k.strip()] = v.strip().strip('"')

API_KEY = env["OPENAI_API_KEY"]
BASE_URL = env["OPENAI_BASE_URL"].rstrip("/")
MODEL = env["OPENAI_MODEL"]
DB = env["MYSQL_DATABASE"]

def connect_db():
    return pymysql.connect(
        host=env["MYSQL_HOST"], port=int(env["MYSQL_PORT"]),
        user=env["MYSQL_USER"], password=env["MYSQL_PASSWORD"],
        database=DB, charset="utf8mb4"
    )

def call_qwen(anecdote_name, original_summary, dynasty_name):
    """请求 Qwen 扩充 summary 到 200-300 字"""
    system = (
        "你是中华典故叙事扩充助手。用户给你一条典故名称、朝代和一两句简述，"
        "你需要把它扩写成 200-300 字的完整叙述。"
        "要求：用讲故事的口吻，交代清楚来龙去脉、关键人物、转折、结果和影响。"
        "不要套话，不要评价，只叙述事实。直接输出叙述文字，不要 markdown，不要添加任何标签。"
    )
    user = (
        f"典故：{anecdote_name}\n朝代：{dynasty_name}\n原简述：{original_summary}\n\n"
        f"请扩写成 200-300 字的完整典故叙述。"
    )

    resp = requests.post(
        f"{BASE_URL}/chat/completions",
        headers={
            "Authorization": f"Bearer {API_KEY}",
            "Content-Type": "application/json",
        },
        json={
            "model": MODEL,
            "temperature": 0.85,
            "messages": [
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
        },
        timeout=60,
    )
    if resp.status_code != 200:
        raise RuntimeError(f"API {resp.status_code}: {resp.text[:200]}")
    data = resp.json()
    content = data["choices"][0]["message"]["content"].strip()
    # 去掉可能的 markdown 包装
    if content.startswith("```"):
        content = content.split("\n", 1)[-1]
        if content.endswith("```"):
            content = content.rsplit("\n", 1)[0]
    return content

def main():
    conn = connect_db()
    c = conn.cursor()

    # 查询需要扩充的记录（summary < 80 字视为过短）
    c.execute(
        "SELECT id, anecdote_name, summary, dynasty_id "
        "FROM anecdote_data "
        "WHERE CHAR_LENGTH(summary) < 80 "
        "ORDER BY id"
    )
    rows = c.fetchall()
    total = len(rows)
    print(f"找到 {total} 条需要扩充的记录")

    if total == 0:
        conn.close()
        return

    # 支持命令行参数：起始 id 和数量
    start_idx = 0
    count = total
    if len(sys.argv) >= 2:
        start_id = int(sys.argv[1])
        for i, r in enumerate(rows):
            if r[0] >= start_id:
                start_idx = i
                break
    if len(sys.argv) >= 3:
        count = int(sys.argv[2])

    dynasty_map = {
        1: "春秋", 2: "战国", 3: "秦", 4: "楚汉", 5: "西汉", 6: "东汉",
        7: "三国", 8: "西晋", 9: "东晋", 10: "南北朝", 11: "隋", 12: "唐",
        13: "五代十国", 14: "北宋", 15: "南宋", 16: "元", 17: "明", 18: "清",
    }

    updated = 0
    failed = 0
    end_idx = min(start_idx + count, total)

    for i in range(start_idx, end_idx):
        row_id, name, old_summary, dynasty_id = rows[i]
        dynasty = dynasty_map.get(dynasty_id, "未知")
        progress = f"[{i+1}/{total}]"
        print(f"{progress} {name} ({len(old_summary)}字) → ", end="", flush=True)

        try:
            new_text = call_qwen(name, old_summary, dynasty)
            new_len = len(new_text)
            c.execute("UPDATE anecdote_data SET summary = %s WHERE id = %s", (new_text, row_id))
            conn.commit()
            updated += 1
            print(f"{new_len}字 ✅")
        except Exception as e:
            failed += 1
            print(f"失败: {e}")

        if i % 10 == 9:
            print(f"--- 已处理 {i+1}/{total}, 成功 {updated}, 失败 {failed} ---")

        time.sleep(0.6)

    print(f"\n完成！成功 {updated} 条, 失败 {failed} 条")
    conn.close()

if __name__ == "__main__":
    main()
