package com.guseeit.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DynastyConstants {

    public static final List<String> SUPPORTED = Collections.unmodifiableList(Arrays.asList(
            "秦", "汉", "三国", "晋", "南北朝", "隋", "唐", "宋", "元", "明", "清", "民国"
    ));

    private static final Map<String, String> YEAR_HINTS = new LinkedHashMap<String, String>();
    private static final Map<String, String> SLUGS = new LinkedHashMap<String, String>();

    static {
        YEAR_HINTS.put("秦", "year_ad 用负数表示公元前，如 -221；time_label 写「秦朝（公元前221年）」");
        YEAR_HINTS.put("汉", "year_ad 为公元年；time_label 含「汉朝（公元xxx年）」");
        YEAR_HINTS.put("唐", "year_ad 为公元年；若有年号填 reign_label（如「天宝元年」）");
        YEAR_HINTS.put("宋", "year_ad 为公元年；可填 reign_label（如「元丰三年」）");
        YEAR_HINTS.put("明", "year_ad 为公元年；可填 reign_label（如「永乐十四年」）");
        YEAR_HINTS.put("清", "year_ad 为公元年；reign_label 必填（如「乾隆三十九年」）");
        YEAR_HINTS.put("民国", "year_ad 为公元年（1912–1949）；time_label 写「民国（公元xxx年）」");

        SLUGS.put("秦", "qin");
        SLUGS.put("汉", "han");
        SLUGS.put("三国", "sanguo");
        SLUGS.put("晋", "jin");
        SLUGS.put("南北朝", "nbchao");
        SLUGS.put("隋", "sui");
        SLUGS.put("唐", "tang");
        SLUGS.put("宋", "song");
        SLUGS.put("元", "yuan");
        SLUGS.put("明", "ming");
        SLUGS.put("清", "qing");
        SLUGS.put("民国", "minguo");
    }

    private DynastyConstants() {
    }

    public static String yearHint(String dynasty) {
        String hint = YEAR_HINTS.get(dynasty);
        return hint != null ? hint : "year_ad 为整数公元年（公元前用负数）";
    }

    public static String slug(String dynasty) {
        String slug = SLUGS.get(dynasty);
        return slug != null ? slug : "era";
    }
}
