package com.guseeit.client;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.guseeit.config.GuseeitProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

@Service
public class OssService {

    private final GuseeitProperties.Oss oss;
    private volatile OSS client;

    public OssService(GuseeitProperties properties) {
        this.oss = properties.getOss();
    }

    public static class UploadResult {
        private final String objectKey;
        private final String imageUrl;

        public UploadResult(String objectKey, String imageUrl) {
            this.objectKey = objectKey;
            this.imageUrl = imageUrl;
        }

        public String getObjectKey() { return objectKey; }
        public String getImageUrl() { return imageUrl; }
    }

    /** 上传图片，用自定义名称作为文件名标识 */
    public UploadResult upload(byte[] data, String name) {
        String slug = name.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "").trim();
        if (slug.length() > 20) slug = slug.substring(0, 20);
        String id = UUID.randomUUID().toString().substring(0, 8);
        String fileName = slug + "-" + id + ".png";
        return doUpload(data, fileName);
    }

    private UploadResult doUpload(byte[] data, String fileName) {
        String prefix = oss.getPrefix() == null ? "guseeit/rounds" : oss.getPrefix().replaceAll("/$", "");
        String objectKey = prefix + "/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType("image/png");
        if ("public-read".equalsIgnoreCase(oss.getObjectAcl())) {
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
        }

        PutObjectRequest request = new PutObjectRequest(
                oss.getBucket(),
                objectKey,
                new ByteArrayInputStream(data),
                metadata
        );

        getClient().putObject(request);

        String publicBase = oss.getPublicBaseUrl() == null ? "" : oss.getPublicBaseUrl().replaceAll("/$", "");
        String imageUrl = publicBase.isEmpty() ? objectKey : publicBase + "/" + encodePath(objectKey);
        return new UploadResult(objectKey, imageUrl);
    }

    private OSS getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new OSSClientBuilder().build(
                            oss.getEndpoint(),
                            oss.getAccessKeyId(),
                            oss.getAccessKeySecret()
                    );
                }
            }
        }
        return client;
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) return;
        try {
            getClient().deleteObject(oss.getBucket(), objectKey);
        } catch (Exception e) {
            // 删除 OSS 对象失败不应阻塞主流程，仅记录
        }
    }

    @PreDestroy
    void shutdown() {
        if (client != null) {
            client.shutdown();
        }
    }

    private static String encodePath(String objectKey) {
        String[] parts = objectKey.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            try {
                sb.append(URLEncoder.encode(parts[i], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                sb.append(parts[i]);
            }
        }
        return sb.toString();
    }
}
