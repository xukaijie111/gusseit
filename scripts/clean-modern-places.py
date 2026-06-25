#!/usr/bin/env python3
"""清洗 rounds 表：补全 / 规范化 modern_place，并同步 geo_query。"""

import json
import os
import re
import sys
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ENV_FILE = ROOT / ".env"

# 常见历史地名 → 现代地级市
HISTORICAL_CITY_MAP = {
    "长安": "西安市",
    "咸阳": "咸阳市",
    "汴京": "开封市",
    "汴梁": "开封市",
    "东京": "开封市",
    "金陵": "南京市",
    "建康": "南京市",
    "江宁": "南京市",
    "临安": "杭州市",
    "钱塘": "杭州市",
    "大都": "北京市",
    "燕京": "北京市",
    "北平": "北京市",
    "洛阳": "洛阳市",
    "神都": "洛阳市",
    "成都": "成都市",
    "益州": "成都市",
    "江州": "重庆市",
    "渝州": "重庆市",
    "广州": "广州市",
    "番禺": "广州市",
    "襄阳": "襄阳市",
    "樊城": "襄阳市",
    "江陵": "荆州市",
    "武昌": "武汉市",
    "汉口": "武汉市",
    "苏州": "苏州市",
    "吴郡": "苏州市",
    "扬州": "扬州市",
    "广陵": "扬州市",
    "太原": "太原市",
    "晋阳": "太原市",
    "大同": "大同市",
    "平城": "大同市",
    "临淄": "淄博市",
    "琅琊": "青岛市",
    "会稽": "绍兴市",
    "山阴": "绍兴市",
    "曲阜": "济宁市",
    "曲阜郡": "济宁市",
    "函谷关": "三门峡市",
    "潼关": "渭南市",
    "嘉峪关": "嘉峪关市",
    "敦煌": "敦煌市",
    "张掖": "张掖市",
    "武威": "武威市",
    "凉州": "武威市",
    "兰州": "兰州市",
    "金城": "兰州市",
    "西宁": "西宁市",
    "鄯州": "西宁市",
    "银川": "银川市",
    "兴庆": "银川市",
    "乌鲁木齐": "乌鲁木齐市",
    "迪化": "乌鲁木齐市",
    "拉萨": "拉萨市",
    "逻些": "拉萨市",
    "昆明": "昆明市",
    "大理": "大理市",
    "桂林": "桂林市",
    "福州": "福州市",
    "闽县": "福州市",
    "厦门": "厦门市",
    "泉州": "泉州市",
    "刺桐": "泉州市",
    "南昌": "南昌市",
    "洪州": "南昌市",
    "长沙": "长沙市",
    "潭州": "长沙市",
    "岳阳": "岳阳市",
    "巴陵": "岳阳市",
    "合肥": "合肥市",
    "庐州": "合肥市",
    "徐州": "徐州市",
    "彭城": "徐州市",
    "南阳": "南阳市",
    "宛城": "南阳市",
    "许昌": "许昌市",
    "邺城": "邯郸市",
    "邯郸": "邯郸市",
    "秦皇岛": "秦皇岛市",
    "山海关": "秦皇岛市",
    "包头": "包头市",
    "九原": "包头市",
    "呼和浩特": "呼和浩特市",
    "归绥": "呼和浩特市",
    "沈阳": "沈阳市",
    "盛京": "沈阳市",
    "大连": "大连市",
    "旅顺": "大连市",
    "长春": "长春市",
    "哈尔滨": "哈尔滨市",
    "齐齐哈尔": "齐齐哈尔市",
    "香港": "香港特别行政区",
    "澳门": "澳门特别行政区",
}


def load_env():
    env = {}
    if not ENV_FILE.exists():
        print(f"缺少 {ENV_FILE}", file=sys.stderr)
        sys.exit(1)
    for line in ENV_FILE.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        env[k.strip()] = v.strip()
    return env


def format_city_label(name):
    if not name:
        return None
    s = name.strip()
    if not s:
        return None
    if "省" in s:
        s = s.split("省")[-1].strip()
    if "自治区" in s:
        s = s.split("自治区")[-1].strip()
    if s.endswith("特别行政区"):
        return s
    if s.endswith(("市", "县", "区")) or len(s) > 4:
        return s
    return s + "市"


def is_valid_modern_city(name):
    if not name:
        return False
    s = name.strip()
    if not s or s == "未知地区":
        return False
    return s.endswith("市") or s.endswith("特别行政区")


def guess_from_location_name(location_name):
    if not location_name:
        return None
    text = location_name.strip()

    m = re.search(r"今([^（）()\\s，,]+?市)", text)
    if m:
        return format_city_label(m.group(1))

    for key in sorted(HISTORICAL_CITY_MAP.keys(), key=len, reverse=True):
        if key in text:
            return HISTORICAL_CITY_MAP[key]

    return None


def amap_geocode(address, amap_key, base_url):
    if not amap_key or not address:
        return None
    base = base_url.rstrip("/")
    qs = urllib.parse.urlencode(
        {"key": amap_key, "address": address},
        quote_via=urllib.parse.quote,
    )
    url = f"{base}/geocode/geo?{qs}"
    try:
        with urllib.request.urlopen(url, timeout=15) as resp:
            data = json.loads(resp.read().decode())
    except Exception as e:
        print(f"  [warn] 高德请求失败: {address} ({e})")
        return None
    if data.get("status") != "1":
        return None
    geocodes = data.get("geocodes") or []
    if not geocodes:
        return None
    best = geocodes[0]
    for item in geocodes:
        level = item.get("level") or ""
        if level == "市" or "市" in level:
            best = item
            break
    city = best.get("city")
    if isinstance(city, list):
        city = city[0] if city else ""
    city = (city or "").strip()
    if city and city != "[]":
        return format_city_label(city)
    formatted = (best.get("formatted_address") or "").strip()
    if formatted:
        m = re.search(r"([\u4e00-\u9fff]+?市)", formatted)
        if m:
            return format_city_label(m.group(1))
    return None


def resolve_modern_place(row, amap_key, amap_base):
    location_name = (row.get("location_name") or "").strip()
    modern_place = (row.get("modern_place") or "").strip()
    geo_query = (row.get("geo_query") or "").strip()

    if is_valid_modern_city(modern_place):
        return format_city_label(modern_place), "已有"

    for candidate in [geo_query, modern_place]:
        labeled = format_city_label(candidate)
        if is_valid_modern_city(labeled):
            return labeled, "geo_query/modern_place"

    guessed = guess_from_location_name(location_name)
    if is_valid_modern_city(guessed):
        return guessed, "历史地名映射"

    for query in [location_name, location_name + ",中国"]:
        resolved = amap_geocode(query, amap_key, amap_base)
        if is_valid_modern_city(resolved):
            return resolved, f"高德({query})"

    return None, "无法推断"


def main():
    try:
        import pymysql
    except ImportError:
        print("请先安装: pip3 install pymysql", file=sys.stderr)
        sys.exit(1)

    env = load_env()
    amap_key = env.get("AMAP_WEB_KEY", "")
    amap_base = env.get("AMAP_BASE_URL", "https://restapi.amap.com/v3")

    conn = pymysql.connect(
        host=env.get("MYSQL_HOST", "127.0.0.1"),
        port=int(env.get("MYSQL_PORT", "3306")),
        user=env.get("MYSQL_USER", "root"),
        password=env.get("MYSQL_PASSWORD", ""),
        database=env.get("MYSQL_DATABASE", "guseeit"),
        charset="utf8mb4",
    )

    with conn.cursor(pymysql.cursors.DictCursor) as cur:
        cur.execute(
            "SELECT id, location_name, modern_place, geo_query FROM rounds ORDER BY created_at"
        )
        rows = cur.fetchall()

    updated = 0
    skipped = 0
    failed = []

    print(f"共 {len(rows)} 条题目，开始清洗 modern_place / geo_query …")

    with conn.cursor() as cur:
        for row in rows:
            rid = row["id"]
            old_modern = (row.get("modern_place") or "").strip()
            old_geo = (row.get("geo_query") or "").strip()
            location = (row.get("location_name") or "").strip()

            new_modern, reason = resolve_modern_place(row, amap_key, amap_base)
            if not new_modern:
                failed.append({"id": rid, "location_name": location})
                print(f"  ✗ {location or rid} — 无法补全现代城市名")
                continue

            new_modern = format_city_label(new_modern)
            new_geo = new_modern

            if old_modern == new_modern and old_geo == new_geo:
                skipped += 1
                continue

            cur.execute(
                "UPDATE rounds SET modern_place=%s, geo_query=%s, updated_at=NOW() WHERE id=%s",
                (new_modern, new_geo, rid),
            )
            updated += 1
            print(
                f"  ✓ {location}: {old_modern or '(空)'} → {new_modern} [{reason}]"
            )

    conn.commit()
    conn.close()

    print(f"\n完成：更新 {updated} 条，已规范 {skipped} 条，失败 {len(failed)} 条")
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    main()
