package com.guseeit.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guseeit.config.GuseeitProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GeocodeClient {

    private final GuseeitProperties.Amap amap;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, double[]> forwardCache = new ConcurrentHashMap<String, double[]>();
    private final ConcurrentHashMap<String, CityPoint> reverseCache = new ConcurrentHashMap<String, CityPoint>();
    private final ConcurrentHashMap<String, CityPoint> cityCache = new ConcurrentHashMap<String, CityPoint>();

    public GeocodeClient(GuseeitProperties properties) {
        this.amap = properties.getAmap();
    }

    public double[] resolve(String geoQuery, String modernPlace) {
        CityPoint city = resolveCity(geoQuery, modernPlace);
        if (city == null) {
            return null;
        }
        return new double[]{city.getLatitude(), city.getLongitude()};
    }

    public CityPoint resolveCity(String geoQuery, String modernPlace) {
        String query = buildQuery(geoQuery, modernPlace);
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        CityPoint cached = cityCache.get(query);
        if (cached != null) {
            return cached;
        }

        double[] coords = forwardSearch(query);
        if (coords == null) {
            return null;
        }

        String name = displayPlaceName(modernPlace, geoQuery);
        CityPoint city = new CityPoint(name, coords[0], coords[1]);
        cityCache.put(query, city);
        return city;
    }

    /** 按现代地名解析城市中心（计分答案侧统一走此入口） */
    public CityPoint resolveModernCity(String modernPlace) {
        return resolveCityCenter(modernPlace);
    }

    /**
     * 解析城市中心坐标，依次尝试多种查询词（含 modern_place / 城市名 / 带市后缀）。
     */
    public CityPoint resolveCityCenter(String cityLabel) {
        if (cityLabel == null || cityLabel.trim().isEmpty()) {
            return null;
        }
        String displayName = formatCityLabel(cityLabel.trim());
        for (String query : buildCityQueries(cityLabel.trim())) {
            CityPoint cached = cityCache.get(query);
            if (cached != null) {
                return new CityPoint(displayName, cached.getLatitude(), cached.getLongitude());
            }
            double[] coords = forwardSearch(query);
            if (coords == null) {
                coords = forwardSearchDistrict(query);
            }
            if (coords != null) {
                CityPoint city = new CityPoint(displayName, coords[0], coords[1]);
                cityCache.put(query, city);
                return city;
            }
        }
        return null;
    }

    private static String[] buildCityQueries(String label) {
        String formatted = formatCityLabel(label);
        java.util.LinkedHashSet<String> queries = new java.util.LinkedHashSet<String>();
        queries.add(formatted);
        queries.add(label.trim());
        if (formatted.endsWith("市")) {
            queries.add(formatted.substring(0, formatted.length() - 1));
        }
        return queries.toArray(new String[0]);
    }

    public CityPoint reverseCity(double latitude, double longitude) {
        String cacheKey = "rev:" + Math.round(latitude * 1000) + ":" + Math.round(longitude * 1000);
        CityPoint cached = reverseCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        if (!hasAmapKey()) {
            return new CityPoint("未知地区", latitude, longitude);
        }

        String name = reverseWithAmap(latitude, longitude);
        if (name == null || name.trim().isEmpty()) {
            return new CityPoint("未知地区", latitude, longitude);
        }

        String label = formatCityLabel(name);
        CityPoint center = resolveCityCenter(label);
        CityPoint result = center != null ? center : new CityPoint(label, latitude, longitude);
        reverseCache.put(cacheKey, result);
        return result;
    }

    private boolean hasAmapKey() {
        return amap.getKey() != null && !amap.getKey().trim().isEmpty();
    }

    private String reverseWithAmap(double latitude, double longitude) {
        try {
            String base = amap.getBaseUrl().replaceAll("/$", "");
            String location = longitude + "," + latitude;
            String url = base + "/geocode/regeo?key=" + amap.getKey()
                    + "&location=" + location
                    + "&extensions=base";

            JsonNode root = objectMapper.readTree(restTemplate.getForObject(url, String.class));
            if (!"1".equals(root.path("status").asText())) {
                return null;
            }

            JsonNode component = root.path("regeocode").path("addressComponent");
            return extractAmapCityName(component);
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractAmapCityName(JsonNode component) {
        if (component == null || component.isMissingNode()) {
            return null;
        }

        String city = amapField(component, "city");
        if (isValidAmapField(city)) {
            return city;
        }

        String province = amapField(component, "province");
        if (isValidAmapField(province) && province.endsWith("市")) {
            return province;
        }

        if (isValidAmapField(province)) {
            return province;
        }

        return null;
    }

    private static String amapField(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode()) {
            return "";
        }
        return value.asText("").trim();
    }

    private static boolean isValidAmapField(String value) {
        return value != null && !value.isEmpty() && !"[]".equals(value);
    }

    public static boolean sameCity(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        String ka = normalizeCityKey(a);
        String kb = normalizeCityKey(b);
        if (ka.isEmpty() || kb.isEmpty()) {
            return false;
        }
        return ka.equals(kb);
    }

    public static String normalizeCityKey(String name) {
        if (name == null) {
            return "";
        }
        String s = name.trim();
        int provinceIdx = s.indexOf("省");
        if (provinceIdx >= 0 && provinceIdx < s.length() - 1) {
            s = s.substring(provinceIdx + 1).trim();
        }
        s = s.replace("特别行政区", "");
        s = s.replace("维吾尔自治区", "").replace("壮族自治区", "").replace("回族自治区", "");
        s = s.replace("自治区", "").replace("省", "").replace("市", "").replace("区", "").replace("县", "");
        return s.trim();
    }

    public static String formatCityLabel(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "未知地区";
        }
        String s = name.trim();
        if (!s.endsWith("市") && !s.endsWith("县") && !s.endsWith("区") && s.length() <= 4) {
            return s + "市";
        }
        return s;
    }

    private double[] forwardSearch(String query) {
        double[] cached = forwardCache.get(query);
        if (cached != null) {
            return cached;
        }

        if (!hasAmapKey()) {
            return null;
        }

        try {
            String base = amap.getBaseUrl().replaceAll("/$", "");
            String url = base + "/geocode/geo?key=" + amap.getKey()
                    + "&address=" + encode(query);

            JsonNode root = objectMapper.readTree(httpGet(url));
            String status = root.path("status").asText("");
            if (!"1".equals(status)) {
                return null;
            }

            JsonNode geocodes = root.path("geocodes");
            if (!geocodes.isArray() || geocodes.size() == 0) {
                return null;
            }
            JsonNode best = pickCityLevelGeocode(geocodes);
            if (best == null) {
                return null;
            }

            String location = best.path("location").asText("");
            if (location.isEmpty() || !location.contains(",")) {
                return null;
            }

            String[] parts = location.split(",");
            double lng = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            double[] coords = new double[]{lat, lng};
            forwardCache.put(query, coords);
            return coords;
        } catch (Exception e) {
            return null;
        }
    }

    /** 行政区划 API 兜底，获取城市中心点 */
    private double[] forwardSearchDistrict(String keyword) {
        if (!hasAmapKey()) {
            return null;
        }
        try {
            String base = amap.getBaseUrl().replaceAll("/$", "");
            String url = base + "/config/district?keywords=" + encode(keyword)
                    + "&subdistrict=0&extensions=base&key=" + amap.getKey();

            JsonNode root = objectMapper.readTree(httpGet(url));
            if (!"1".equals(root.path("status").asText())) {
                return null;
            }
            JsonNode districts = root.path("districts");
            if (!districts.isArray() || districts.size() == 0) {
                return null;
            }
            String center = districts.get(0).path("center").asText("");
            if (center.isEmpty() || !center.contains(",")) {
                return null;
            }
            String[] parts = center.split(",");
            double lng = Double.parseDouble(parts[0].trim());
            double lat = Double.parseDouble(parts[1].trim());
            return new double[]{lat, lng};
        } catch (Exception e) {
            return null;
        }
    }

    private static JsonNode pickCityLevelGeocode(JsonNode geocodes) {
        if (geocodes == null || !geocodes.isArray() || geocodes.size() == 0) {
            return null;
        }
        for (int i = 0; i < geocodes.size(); i++) {
            JsonNode item = geocodes.get(i);
            String level = item.path("level").asText("");
            if ("市".equals(level) || level.contains("市")) {
                return item;
            }
        }
        return geocodes.get(0);
    }

    private static String buildQuery(String geoQuery, String modernPlace) {
        if (modernPlace != null && !modernPlace.trim().isEmpty()) {
            return formatCityLabel(modernPlace.trim());
        }
        if (geoQuery != null && !geoQuery.trim().isEmpty()) {
            return geoQuery.trim();
        }
        return null;
    }

    private static String displayPlaceName(String modernPlace, String geoQuery) {
        if (modernPlace != null && !modernPlace.trim().isEmpty()) {
            return formatCityLabel(modernPlace.trim());
        }
        if (geoQuery != null && !geoQuery.trim().isEmpty()) {
            return formatCityLabel(geoQuery.trim());
        }
        return "未知地区";
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (in == null) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) >= 0) {
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public static final class CityPoint {
        private final String name;
        private final double latitude;
        private final double longitude;

        public CityPoint(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
