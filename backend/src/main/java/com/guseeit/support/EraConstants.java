package com.guseeit.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EraConstants {

    public static final String ALL = "";

    private static final Map<String, Era> ERAS = new LinkedHashMap<String, Era>();

    static {
        ERAS.put("proto", new Era(
                "proto",
                "远古",
                "远古",
                "秦 · 汉",
                Arrays.asList("秦", "汉")
        ));
        ERAS.put("classic", new Era(
                "classic",
                "古代",
                "古代",
                "三国至唐",
                Arrays.asList("三国", "晋", "南北朝", "隋", "唐")
        ));
        ERAS.put("empire", new Era(
                "empire",
                "近古",
                "近古",
                "宋元至清",
                Arrays.asList("宋", "元", "明", "清")
        ));
        ERAS.put("modern", new Era(
                "modern",
                "近代",
                "近代",
                "民国",
                Collections.singletonList("民国")
        ));
    }

    private EraConstants() {
    }

    public static List<String> eraKeys() {
        return Collections.unmodifiableList(Arrays.asList("proto", "classic", "empire", "modern"));
    }

    public static List<String> dynastiesFor(String eraKey) {
        if (eraKey == null || eraKey.trim().isEmpty()) {
            return null;
        }
        Era era = ERAS.get(eraKey.trim());
        if (era == null) {
            throw new IllegalArgumentException("无效时代范围");
        }
        return era.getDynasties();
    }

    public static final class Era {
        private final String key;
        private final String ruler;
        private final String title;
        private final String subtitle;
        private final List<String> dynasties;

        public Era(String key, String ruler, String title, String subtitle, List<String> dynasties) {
            this.key = key;
            this.ruler = ruler;
            this.title = title;
            this.subtitle = subtitle;
            this.dynasties = Collections.unmodifiableList(dynasties);
        }

        public String getKey() { return key; }
        public String getRuler() { return ruler; }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public List<String> getDynasties() { return dynasties; }
    }
}
