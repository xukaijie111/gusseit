package com.guseeit.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guseeit.config.GuseeitProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class ArkImageClient {

    private final RestTemplate restTemplate;
    private final GuseeitProperties.Ark ark;
    private final ObjectMapper objectMapper;

    public ArkImageClient(GuseeitProperties properties, ObjectMapper objectMapper) {
        this.ark = properties.getArk();
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public byte[] generateImage(String prompt) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", ark.getModel());
        body.put("prompt", prompt);
        body.put("size", ark.getImageSize());
        body.put("response_format", "url");
        body.put("watermark", false);

        // Seedream 5.0 提示词优化（即梦同款效果）
        Map<String, String> optimize = new HashMap<String, String>();
        optimize.put("mode", "standard");
        body.put("optimize_prompt_options", optimize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ark.getApiKey());

        String url = trimSlash(ark.getBaseUrl()) + "/images/generations";
        ResponseEntity<String> response = restTemplate.postForEntity(
                url, new HttpEntity<Map<String, Object>>(body, headers), String.class
        );

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data0 = root.path("data").path(0);
            String imageUrl = data0.path("url").asText(null);
            String b64 = data0.path("b64_json").asText(null);

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                return downloadBytes(imageUrl);
            }
            if (b64 != null && !b64.trim().isEmpty()) {
                return Base64.getDecoder().decode(b64);
            }
            throw new IllegalStateException("文生图响应无图片: " + response.getBody());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("文生图失败: " + e.getMessage(), e);
        }
    }

    public String getImageSize() {
        return ark.getImageSize();
    }

    private static byte[] downloadBytes(String imageUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(120000);
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IllegalStateException("下载生成图失败 HTTP " + code);
        }
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        in.close();
        return out.toByteArray();
    }

    private static String trimSlash(String url) {
        return url == null ? "" : url.replaceAll("/$", "");
    }
}
