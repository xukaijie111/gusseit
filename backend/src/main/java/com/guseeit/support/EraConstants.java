package com.guseeit.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EraConstants {

    public static final String ALL = "";

    private static final Map<String, Era> ERAS = new LinkedHashMap<>();

    static {
        ERAS.put("preqin",    new Era("preqin",    "先秦",   "先秦",   "春秋 · 战国",
                Arrays.asList(1, 2)));
        ERAS.put("qinhan",    new Era("qinhan",    "秦汉",   "秦汉",   "秦至三国",
                Arrays.asList(3, 4, 5, 6, 7)));
        ERAS.put("weijin",    new Era("weijin",    "魏晋南北朝", "魏晋南北朝", "西晋至隋",
                Arrays.asList(8, 9, 10, 11)));
        ERAS.put("suitang",   new Era("suitang",   "隋唐五代", "隋唐五代", "隋至五代十国",
                Arrays.asList(11, 12, 13)));
        ERAS.put("songyuan",  new Era("songyuan",  "宋元",   "宋元",   "北宋至元",
                Arrays.asList(14, 15, 16)));
        ERAS.put("mingqing",  new Era("mingqing",  "明清",   "明清",   "明 · 清",
                Arrays.asList(17, 18)));
    }

    private EraConstants() {}

    public static List<Era> getAll() {
        List<Era> list = new java.util.ArrayList<>();
        for (String key : eraKeys()) {
            Era e = ERAS.get(key);
            if (e != null) list.add(e);
        }
        return list;
    }

    public static List<String> eraKeys() {
        return Collections.unmodifiableList(Arrays.asList("preqin","qinhan","weijin","suitang","songyuan","mingqing"));
    }

    public static List<Integer> dynastyIdsFor(String eraKey) {
        if (eraKey == null || eraKey.trim().isEmpty()) return null;
        Era era = ERAS.get(eraKey.trim());
        if (era == null) throw new IllegalArgumentException("无效时代范围");
        return era.dynastyIds;
    }

    public static final class Era {
        private final String key, ruler, title, subtitle;
        private final List<Integer> dynastyIds;

        Era(String key, String ruler, String title, String subtitle, List<Integer> dynastyIds) {
            this.key = key; this.ruler = ruler; this.title = title; this.subtitle = subtitle;
            this.dynastyIds = Collections.unmodifiableList(dynastyIds);
        }
        public String getKey() { return key; }
        public String getRuler() { return ruler; }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public List<Integer> getDynastyIds() { return dynastyIds; }
    }
}
