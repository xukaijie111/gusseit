package com.guseeit.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 与 miniprogram/utils/dynasties.js LIST 保持一致 */
public final class TimelineConstants {

    private static final List<Period> PERIODS = Collections.unmodifiableList(Arrays.asList(
            new Period("秦", "秦朝", -221, -206),
            new Period("汉", "汉朝", -206, 220),
            new Period("三国", "三国", 220, 280),
            new Period("晋", "晋朝", 265, 420),
            new Period("南北朝", "南北朝", 420, 589),
            new Period("隋", "隋朝", 581, 618),
            new Period("唐", "唐朝", 618, 907),
            new Period("宋", "宋朝", 960, 1279),
            new Period("元", "元朝", 1271, 1368),
            new Period("明", "明朝", 1368, 1644),
            new Period("清", "清朝", 1644, 1912),
            new Period("民国", "民国", 1912, 1949)
    ));

    private static final Map<String, String> TITLES = new LinkedHashMap<String, String>();

    static {
        for (int i = 0; i < PERIODS.size(); i++) {
            Period p = PERIODS.get(i);
            TITLES.put(p.key, p.title);
        }
    }

    private TimelineConstants() {
    }

    public static String dynastyAt(int year) {
        for (int i = 0; i < PERIODS.size(); i++) {
            Period p = PERIODS.get(i);
            if (year >= p.start && year <= p.end) {
                return p.key;
            }
        }
        return "";
    }

    public static String dynastyTitle(String dynastyKey) {
        if (dynastyKey == null || dynastyKey.trim().isEmpty()) {
            return "";
        }
        String title = TITLES.get(dynastyKey.trim());
        return title != null ? title : dynastyKey.trim();
    }

    private static final class Period {
        private final String key;
        private final String title;
        private final int start;
        private final int end;

        private Period(String key, String title, int start, int end) {
            this.key = key;
            this.title = title;
            this.start = start;
            this.end = end;
        }
    }
}
