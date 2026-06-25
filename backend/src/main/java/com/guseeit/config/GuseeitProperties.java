package com.guseeit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "guseeit")
public class GuseeitProperties {

    private String corsOrigins = "http://localhost:5173";
    private Qwen qwen = new Qwen();
    private Ark ark = new Ark();
    private Oss oss = new Oss();
    private Amap amap = new Amap();

    public String getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    public Qwen getQwen() {
        return qwen;
    }

    public void setQwen(Qwen qwen) {
        this.qwen = qwen;
    }

    public Ark getArk() {
        return ark;
    }

    public void setArk(Ark ark) {
        this.ark = ark;
    }

    public Oss getOss() {
        return oss;
    }

    public void setOss(Oss oss) {
        this.oss = oss;
    }

    public Amap getAmap() {
        return amap;
    }

    public void setAmap(Amap amap) {
        this.amap = amap;
    }

    public static class Amap {
        private String key;
        private String baseUrl = "https://restapi.amap.com/v3";

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Qwen {
        private String apiKey;
        private String baseUrl;
        private String model;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class Ark {
        private String apiKey;
        private String baseUrl;
        private String model;
        private String imageSize;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getImageSize() {
            return imageSize;
        }

        public void setImageSize(String imageSize) {
            this.imageSize = imageSize;
        }
    }

    public static class Oss {
        private String endpoint;
        private String bucket;
        private String publicBaseUrl;
        private String prefix;
        private String accessKeyId;
        private String accessKeySecret;
        private String objectAcl;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getObjectAcl() {
            return objectAcl;
        }

        public void setObjectAcl(String objectAcl) {
            this.objectAcl = objectAcl;
        }
    }
}
