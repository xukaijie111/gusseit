package com.guseeit.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TimelineConstants {

    private static final List<Period> PERIODS = Collections.unmodifiableList(Arrays.asList(
            new Period("春秋",   "春秋",   -770, -476),
            new Period("战国",   "战国",   -475, -221),
            new Period("秦",     "秦朝",   -221, -206),
            new Period("楚汉",   "楚汉",   -206, -202),
            new Period("西汉",   "西汉",   -202, 8),
            new Period("东汉",   "东汉",   25, 220),
            new Period("三国",   "三国",   220, 280),
            new Period("西晋",   "西晋",   265, 316),
            new Period("东晋",   "东晋",   317, 420),
            new Period("南北朝", "南北朝", 420, 589),
            new Period("隋",     "隋朝",   581, 618),
            new Period("唐",     "唐朝",   618, 907),
            new Period("五代十国","五代十国",907, 960),
            new Period("北宋",   "北宋",   960, 1127),
            new Period("南宋",   "南宋",   1127, 1279),
            new Period("元",     "元朝",   1271, 1368),
            new Period("明",     "明朝",   1368, 1644),
            new Period("清",     "清朝",   1644, 1912)
    ));

    private static final Map<String, String> TITLES = new LinkedHashMap<>();

    static {
        for (Period p : PERIODS) TITLES.put(p.key, p.title);
    }

    private TimelineConstants() {}

    public static String dynastyAt(int year) {
        for (Period p : PERIODS) {
            if (year >= p.start && year <= p.end) return p.key;
        }
        return "";
    }

    public static String dynastyTitle(String dynastyKey) {
        if (dynastyKey == null || dynastyKey.trim().isEmpty()) return "";
        String t = TITLES.get(dynastyKey.trim());
        return t != null ? t : dynastyKey.trim();
    }

    public static List<Period> getAllPeriods() {
        return PERIODS;
    }

    public static final class Period {
        private final String key, title;
        private final int start, end;
        Period(String key, String title, int start, int end) {
            this.key = key; this.title = title; this.start = start; this.end = end;
        }
        public String getKey() { return key; }
        public String getTitle() { return title; }
        public int getStart() { return start; }
        public int getEnd() { return end; }
    }
}
