package com.guseeit.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DynastyConstants {

    public static final Dynasty CHUNQIU   = new Dynasty(1,  "春秋",     "chunqiu");
    public static final Dynasty ZHANGUO   = new Dynasty(2,  "战国",     "zhanguo");
    public static final Dynasty QIN       = new Dynasty(3,  "秦",       "qin");
    public static final Dynasty CHUHAN    = new Dynasty(4,  "楚汉",     "chuhan");
    public static final Dynasty XIHAN     = new Dynasty(5,  "西汉",     "xihan");
    public static final Dynasty DONGHAN   = new Dynasty(6,  "东汉",     "donghan");
    public static final Dynasty SANGUO    = new Dynasty(7,  "三国",     "sanguo");
    public static final Dynasty XIJIN     = new Dynasty(8,  "西晋",     "xijin");
    public static final Dynasty DONGJIN   = new Dynasty(9,  "东晋",     "dongjin");
    public static final Dynasty NBCHAO    = new Dynasty(10, "南北朝",   "nbchao");
    public static final Dynasty SUI       = new Dynasty(11, "隋",       "sui");
    public static final Dynasty TANG      = new Dynasty(12, "唐",       "tang");
    public static final Dynasty WUDAI     = new Dynasty(13, "五代十国", "wudai");
    public static final Dynasty BEISONG   = new Dynasty(14, "北宋",     "beisong");
    public static final Dynasty NANSONG   = new Dynasty(15, "南宋",     "nansong");
    public static final Dynasty YUAN      = new Dynasty(16, "元",       "yuan");
    public static final Dynasty MING      = new Dynasty(17, "明",       "ming");
    public static final Dynasty QING      = new Dynasty(18, "清",       "qing");

    public static final List<Dynasty> SUPPORTED = Collections.unmodifiableList(Arrays.asList(
            CHUNQIU, ZHANGUO, QIN, CHUHAN, XIHAN, DONGHAN, SANGUO,
            XIJIN, DONGJIN, NBCHAO, SUI, TANG, WUDAI,
            BEISONG, NANSONG, YUAN, MING, QING
    ));

    private static final Map<String, Integer> NAME_TO_ID = new LinkedHashMap<>();
    private static final Map<Integer, String> ID_TO_NAME = new LinkedHashMap<>();
    private static final Map<Integer, String> ID_TO_SLUG = new LinkedHashMap<>();

    static {
        for (Dynasty d : SUPPORTED) {
            NAME_TO_ID.put(d.getName(), d.getId());
            ID_TO_NAME.put(d.getId(), d.getName());
            ID_TO_SLUG.put(d.getId(), d.getSlug());
        }
    }

    private DynastyConstants() {}

    public static Integer toId(String name) {
        if (name == null) return null;
        return NAME_TO_ID.get(name.trim());
    }

    public static String toName(Integer id) {
        if (id == null) return null;
        return ID_TO_NAME.get(id);
    }

    public static String slug(Integer id) {
        if (id == null) return "era";
        String s = ID_TO_SLUG.get(id);
        return s != null ? s : "era";
    }

    public static String yearHint(Integer dynastyId) {
        String name = toName(dynastyId);
        if (name == null) return "year_ad 为整数公元年（公元前用负数）";
        switch (name) {
            case "秦": return "year_ad 用负数表示公元前，如 -221";
            case "楚汉": return "year_ad 用负数表示公元前，约 -206 至 -202";
            case "西汉": return "year_ad 公元年（-202 至 8）";
            case "东汉": return "year_ad 公元年（25–220）";
            case "三国": return "year_ad 公元年（220–280）";
            case "唐": return "year_ad 为公元年；若有年号填 reign_label（如「天宝元年」）";
            case "北宋": return "year_ad 为公元年；可填 reign_label（如「熙宁三年」）";
            case "南宋": return "year_ad 为公元年；可填 reign_label（如「绍兴十年」）";
            case "明": return "year_ad 为公元年；可填 reign_label（如「永乐十四年」）";
            case "清": return "year_ad 为公元年；reign_label 必填（如「乾隆三十九年」）";
            default: return "year_ad 为整数公元年（公元前用负数）";
        }
    }

    public static final class Dynasty {
        private final int id;
        private final String name;
        private final String slug;

        private Dynasty(int id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
    }
}
