package com.guseeit.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

@Component
public class DouyinClient {

    private static final Logger log = LoggerFactory.getLogger(DouyinClient.class);

    @Value("${douyin.app-id:}")
    private String appId;

    @Value("${douyin.app-secret:}")
    private String appSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class SessionInfo {
        private String openid;
        private String sessionKey;
        private String unionid;

        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }

        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }

        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
    }

    public SessionInfo code2Session(String code) {
        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            log.warn("抖音 AppID/Secret 未配置，跳过登录");
            return null;
        }
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String urlStr = "https://developer.toutiao.com/api/apps/v2/jscode2session";
                ObjectNode reqBody = objectMapper.createObjectNode();
                reqBody.put("appid", appId);
                reqBody.put("secret", appSecret);
                reqBody.put("code", code);
                String reqJson = objectMapper.writeValueAsString(reqBody);

                String respBody = httpPostJson(urlStr, reqJson);
                log.info("jscode2session 返回: {}", respBody);
                JsonNode root = objectMapper.readTree(respBody);
                int errNo = root.path("err_no").asInt(-1);
                if (errNo != 0) {
                    log.warn("jscode2session 失败: err_no={} err_tips={}", errNo, root.path("err_tips").asText(""));
                    return null;
                }
                JsonNode data = root.path("data");
                SessionInfo info = new SessionInfo();
                info.openid = data.path("openid").asText("");
                info.sessionKey = data.path("session_key").asText("");
                info.unionid = data.path("unionid").asText("");
                log.info("登录成功 openid={}", info.openid);
                return info;
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn("jscode2session 第{}次重试: {}", attempt, e.getMessage());
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    log.error("jscode2session 异常，已重试{}次", maxRetries, e);
                }
            }
        }
        return null;
    }

    public String decryptPhone(String encryptedData, String iv, String sessionKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] encBytes = Base64.getDecoder().decode(encryptedData);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(encBytes);

            String json = new String(decrypted, StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(json);
            return root.path("phoneNumber").asText("");
        } catch (Exception e) {
            return null;
        }
    }

    private String httpPostJson(String urlStr, String json) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        int code = conn.getResponseCode();
        InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (in == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
