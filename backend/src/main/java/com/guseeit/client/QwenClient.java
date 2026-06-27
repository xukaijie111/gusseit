package com.guseeit.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guseeit.config.GuseeitProperties;
import com.guseeit.dto.AnecdoteItemDto;
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
import java.util.Set;

@Component
public class QwenClient {

    private final RestTemplate restTemplate;
    private final GuseeitProperties.Qwen qwen;
    private final ObjectMapper objectMapper;

    public QwenClient(GuseeitProperties properties, ObjectMapper objectMapper) {
        this.qwen = properties.getQwen();
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    // ===== 典故数据生成结果 =====

    public static final class AnecdoteGenerationResult {
        private final List<AnecdoteItemDto> items;
        private final List<String> errors;

        public AnecdoteGenerationResult(List<AnecdoteItemDto> items, List<String> errors) {
            this.items = items;
            this.errors = errors;
        }
        public List<AnecdoteItemDto> getItems() { return items; }
        public List<String> getErrors() { return errors; }
    }

    // ===== 典故数据生成 =====

    /**
     * 生成典故数据列表（不生成图片提示词，仅元数据）。
     * 单条缺陷不中止全批，跳过并记录错误信息。
     */
    public AnecdoteGenerationResult generateAnecdotes(String dynasty, int count, Set<String> existingNames) {
        String exclusion = buildAnecdoteExclusion(existingNames);
        String systemPrompt = buildAnecdoteSystemPrompt();
        String userPrompt = String.format(
                "请生成 %d 个「%s」的典故条目。%n%s%n%n"
                        + "要求：不与上述排除列表中的典故名称重复；同批次内 modern_city 尽量多样化。%n"
                        + "只输出 JSON，格式为 { \"anecdotes\": [...] }，数组共 %d 条。",
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
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<Map<String, Object>>(body, headers), String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalStateException("千问未返回内容");
            }

            JsonNode parsed = objectMapper.readTree(content);
            JsonNode anecdotesNode = parsed.isArray() ? parsed : parsed.path("anecdotes");
            if (!anecdotesNode.isArray() || anecdotesNode.size() == 0) {
                throw new IllegalStateException("千问返回格式错误");
            }

            List<AnecdoteItemDto> valid = new ArrayList<AnecdoteItemDto>();
            List<String> errors = new ArrayList<String>();
            for (int i = 0; i < anecdotesNode.size(); i++) {
                AnecdoteItemDto dto = objectMapper.treeToValue(anecdotesNode.get(i), AnecdoteItemDto.class);
                String err = validateAnecdote(dto, dynasty, i);
                if (err != null) {
                    errors.add(err);
                } else {
                    valid.add(dto);
                }
            }
            return new AnecdoteGenerationResult(valid, errors);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("解析千问响应失败: " + e.getMessage(), e);
        }
    }

    private static String buildAnecdoteSystemPrompt() {
        return "你是中华历史典故数据生成助手。%n"
                + "必须严格遵守：%n"
                + "1. dynasty_name：朝代名，如「秦」「汉」「三国」「唐」「宋」「清」。%n"
                + "2. anecdote_name：典故名称，4～12 字，如「烽火戏诸侯」「卧薪尝胆」「三顾茅庐」。%n"
                + "3. summary：典故简述，300 字左右，用讲故事的口吻详细叙述起因、关键人物、转折过程、结果。%n"
                + "4. historical_place：当时所属地方（古地名），如「镐京」「洛邑」「赤壁」。%n"
                + "5. modern_location：现代所属地方（具体地点），如「西安市长安区镐京遗址」「湖北省咸宁市赤壁市」。%n"
                + "6. modern_city：现代所属的市，格式如「陕西省西安市」「河南省洛阳市」，必须是真实存在的中国地级市或直辖市。%n"
                + "7. 只输出 JSON，不要 markdown。%n"
                + "JSON 字段：dynasty_name, anecdote_name, summary, historical_place, modern_location, modern_city%n"
                + "输出格式：anecdotes 数组。";
    }

    private static String buildAnecdoteExclusion(Set<String> existingNames) {
        if (existingNames == null || existingNames.isEmpty()) {
            return "暂无已有典故，请自由生成。";
        }
        StringBuilder sb = new StringBuilder("以下典故名称已存在于数据库中，严禁再次生成：\n");
        int i = 0;
        for (String name : existingNames) {
            if (i >= 300) break;
            sb.append("- ").append(name).append("\n");
            i++;
        }
        return sb.toString();
    }

    private static String validateAnecdote(AnecdoteItemDto dto, String dynasty, int index) {
        if (dto.getDynastyName() == null || dto.getAnecdoteName() == null || dto.getSummary() == null
                || dto.getHistoricalPlace() == null || dto.getModernLocation() == null
                || dto.getModernCity() == null) {
            return "第 " + (index + 1) + " 条缺少必填字段";
        }
        if (!dynasty.equals(dto.getDynastyName())) {
            return "第 " + (index + 1) + " 条朝代应为 " + dynasty + "，实际为 " + dto.getDynastyName();
        }
        String name = dto.getAnecdoteName().trim();
        if (name.isEmpty()) {
            return "第 " + (index + 1) + " 条典故名称为空";
        }
        dto.setAnecdoteName(name);
        String summary = dto.getSummary().trim();
        int len = summary.length();
        if (len < 100 || len > 500) {
            return "第 " + (index + 1) + " 条「" + name + "」summary 须为 100–500 字（当前 " + len + " 字）";
        }
        dto.setSummary(summary);
        return null;
    }

    private static String trimSlash(String url) {
        return url == null ? "" : url.replaceAll("/$", "");
    }
}
