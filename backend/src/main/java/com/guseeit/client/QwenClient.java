package com.guseeit.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guseeit.config.GuseeitProperties;
import com.guseeit.domain.Round;
import com.guseeit.dto.RoundPromptDto;
import com.guseeit.support.DynastyConstants;
import com.guseeit.client.GeocodeClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class QwenClient {

    private static final String EXAMPLE_CASE =
            "【时间标注】西周（公元前781年，周幽王时期）\n"
                    + "【地点】骊山烽火台（今西安市）\n"
                    + "【朝代】周\n"
                    + "historical_city: 镐京\n"
                    + "modern_place: 西安市\n"
                    + "geo_query: 西安市\n"
                    + "location_name: 骊山烽火台\n"
                    + "scene_type: 烽火戏诸侯\n"
                    + "knowledge_summary: 周幽王为博褒姒一笑，竟下令点燃骊山烽火台。各路诸侯见烽火以为犬戎入侵，纷纷率兵驰援，到了骊山却发现并无敌情，只有周幽王与褒姒在城头戏弄众人。褒姒终于展露笑颜，幽王却因此失信于天下诸侯。后来犬戎真的来犯，再燃烽火却无人再来救援，西周由此灭亡。这一典故成为「戏弄信任、自毁长城」的历史镜鉴。\n"
                    + "【提示词】360度等距圆柱全景图，equirectangular panorama，2:1 画幅，西周骊山烽火台夜战场景，周幽王与褒姒立于高台，诸侯兵马从四面八方赶来烟尘滚滚，烽火烈焰照亮夜空，古代铠甲与战旗，无现代建筑、汽车、电线、路灯，戏剧性火光与夜色对比，全景四周无缝衔接，写实古风摄影，8K高清；禁止人脸畸形、现代元素、画面裂痕、水印文字。";

    private final RestTemplate restTemplate;
    private final GuseeitProperties.Qwen qwen;
    private final ObjectMapper objectMapper;

    public QwenClient(GuseeitProperties properties, ObjectMapper objectMapper) {
        this.qwen = properties.getQwen();
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public List<RoundPromptDto> generatePrompts(String dynasty, int count, List<Round> existing) {
        String exclusion = buildExclusionBlock(existing, dynasty);
        String systemPrompt = buildSystemPrompt(dynasty);
        String userPrompt = String.format(
                "请生成 %d 个「%s」朝代关卡。%n%n%s%n%n"
                        + "要求：与已有题目的时间+地点组合完全不同；同批次内 modern_place 互不重复。%n"
                        + "禁止人物扭曲，场景撕裂%n"
                        + "输出 JSON 对象，rounds 数组共 %d 条。",
                count, dynasty, exclusion, count
        );

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", qwen.getModel());
        body.put("temperature", 0.85);
        Map<String, String> responseFormat = new HashMap<String, String>();
        responseFormat.put("type", "json_object");
        body.put("response_format", responseFormat);

        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        Map<String, String> systemMsg = new HashMap<String, String>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        Map<String, String> userMsg = new HashMap<String, String>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(systemMsg);
        messages.add(userMsg);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + qwen.getApiKey());

        String url = trimSlash(qwen.getBaseUrl()) + "/chat/completions";
        ResponseEntity<String> response = restTemplate.postForEntity(
                url, new HttpEntity<Map<String, Object>>(body, headers), String.class
        );

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalStateException("千问未返回内容");
            }

            JsonNode parsed = objectMapper.readTree(content);
            JsonNode roundsNode = parsed.isArray() ? parsed : parsed.path("rounds");
            if (!roundsNode.isArray() || roundsNode.size() == 0) {
                throw new IllegalStateException("千问返回格式错误");
            }

            List<RoundPromptDto> rounds = new ArrayList<RoundPromptDto>();
            for (int i = 0; i < roundsNode.size(); i++) {
                RoundPromptDto dto = objectMapper.treeToValue(roundsNode.get(i), RoundPromptDto.class);
                validateRound(dto, dynasty, i);
                rounds.add(dto);
            }
            return rounds;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("解析千问响应失败: " + e.getMessage(), e);
        }
    }

    private void validateRound(RoundPromptDto dto, String dynasty, int index) {
        if (dto.getDynasty() == null || dto.getLocationName() == null || dto.getModernPlace() == null
                || dto.getGeoQuery() == null || dto.getYearAd() == null || dto.getTimeLabel() == null
                || dto.getPrompt() == null || dto.getPrompt().trim().isEmpty()) {
            throw new IllegalStateException("第 " + (index + 1) + " 条缺少必填字段");
        }
        if (!dynasty.equals(dto.getDynasty())) {
            throw new IllegalStateException("第 " + (index + 1) + " 条朝代应为 " + dynasty);
        }
        String modern = GeocodeClient.formatCityLabel(dto.getModernPlace().trim());
        if (modern.equals("未知地区")) {
            throw new IllegalStateException("第 " + (index + 1) + " 条 modern_place 无法识别为城市");
        }
        dto.setModernPlace(modern);
        dto.setGeoQuery(modern);
        if (dto.getHistoricalCity() == null || dto.getHistoricalCity().trim().isEmpty()) {
            throw new IllegalStateException("第 " + (index + 1) + " 条缺少 historical_city（历史城市名）");
        }
        String historicalCity = dto.getHistoricalCity().trim();
        if (historicalCity.endsWith("市") || historicalCity.endsWith("县") || historicalCity.endsWith("区")) {
            throw new IllegalStateException("第 " + (index + 1) + " 条 historical_city 应为历史城市名，不含市/县/区后缀（如长安、洛阳）");
        }
        dto.setHistoricalCity(historicalCity);
        if (dto.getSceneType() == null || dto.getSceneType().trim().isEmpty()) {
            throw new IllegalStateException("第 " + (index + 1) + " 条缺少 scene_type（历史典故名称）");
        }
        dto.setSceneType(dto.getSceneType().trim());
        if (dto.getKnowledgeSummary() == null || dto.getKnowledgeSummary().trim().isEmpty()) {
            throw new IllegalStateException("第 " + (index + 1) + " 条缺少 knowledge_summary（历史典故正文）");
        }
        String anecdote = dto.getKnowledgeSummary().trim();
        int len = anecdote.length();
        if (len < 90 || len > 220) {
            throw new IllegalStateException("第 " + (index + 1) + " 条 knowledge_summary 须为 100～200 字左右（当前 " + len + " 字）");
        }
        dto.setKnowledgeSummary(anecdote);
    }

    private String buildSystemPrompt(String dynasty) {
        return buildSystemPromptForDynasty(dynasty);
    }

    public static String buildSystemPromptForDynasty(String dynasty) {
        String yearHint = DynastyConstants.yearHint(dynasty);
        return String.format(
                "你是中国历史时空解谜游戏的关卡策划。批量生成全景图生图提示词及标准答案 metadata。%n"
                        + "必须严格遵守：%n"
                        + "1. 每条关卡 dynasty 必须为「%s」。%n"
                        + "2. 地名分层（三者必填且含义不同）：%n"
                        + "   - historical_city：历史城市名，如「长安」「洛阳」「汴京」，用于答题展示；%n"
                        + "   - modern_place：现代地名，如「西安」「西安市」「洛阳」「洛阳市」「许昌」「许昌市」，用于高德地图 geocode 计算距离；geo_query 必须与 modern_place 完全一致；%n"
                        + "   - location_name：具体历史场景地点（建筑、关隘、桥、战场等），如「骊山烽火台」「天津桥」「函谷关」，不要与城市名重复。%n"
                        + "3. 每条关卡必须围绕一个真实、著名的历史典故（如烽火戏诸侯、卧薪尝胆、三顾茅庐、草船借箭、破釜沉舟等），scene_type 填典故简称（4～12 字）。%n"
                        + "4. prompt 为完整文生图提示词（一段文字），须将典故的关键场景可视化：写出具体人物、动作、环境、氛围，含 360度等距圆柱全景图、equirectangular panorama、2:1 画幅、无现代元素、全景无缝衔接；需要排除的内容也写在同一段里。%n"
                        + "5. 时间规则：%s%n"
                        + "6. knowledge_summary 必填：100～200 字的历史典故正文，用讲故事的口吻叙述来龙去脉与结局，可自然带出地点，不要 markdown，不要只写地点百科介绍。%n"
                        + "7. 同批次 modern_place 不得重复，典故不得重复 scene_type，场景类型多样化。%n"
                        + "8. 只输出 JSON，不要 markdown。%n"
                        + "JSON 字段：dynasty, historical_city, modern_place, geo_query, location_name, year_ad, reign_label, time_label, prompt, scene_type, knowledge_summary%n"
                        + "参考案例：%n%s%n"
                        + "输出格式：rounds 数组。",
                dynasty, yearHint, EXAMPLE_CASE
        );
    }

    static String buildExclusionBlock(List<Round> existing, String dynasty) {
        List<Round> filtered = new ArrayList<Round>();
        for (Round r : existing) {
            if (dynasty.equals(r.getDynasty())) {
                filtered.add(r);
            }
        }
        if (filtered.isEmpty()) {
            return "数据库中暂无同朝代已有题目，但仍须保证地点+年份互不重复。";
        }

        StringBuilder sb = new StringBuilder(
                "以下「朝代+地点+公元年」组合已在题库中，严禁再次生成（地点或年份必须不同）：\n"
        );
        int limit = Math.min(filtered.size(), 200);
        for (int i = 0; i < limit; i++) {
            Round r = filtered.get(i);
            sb.append("- ")
                    .append(r.getDynasty()).append(" · ")
                    .append(r.getLocationName()).append(" · 公元")
                    .append(r.getYearAd()).append("年（")
                    .append(r.getTimeLabel()).append("）\n");
        }
        return sb.toString();
    }

    private static String trimSlash(String url) {
        return url == null ? "" : url.replaceAll("/$", "");
    }
}
