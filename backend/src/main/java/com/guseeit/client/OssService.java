package com.guseeit.client;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.guseeit.config.GuseeitProperties;
import com.guseeit.dto.RoundPromptDto;
import com.guseeit.support.DynastyConstants;
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

    public UploadResult upload(byte[] data, RoundPromptDto meta) {
        String fileName = buildObjectFileName(meta);
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

    @PreDestroy
    void shutdown() {
        if (client != null) {
            client.shutdown();
        }
    }

    static String buildObjectFileName(RoundPromptDto meta) {
        String dynasty = DynastyConstants.slug(meta.getDynasty());
        int year = Math.abs(meta.getYearAd() == null ? 0 : meta.getYearAd());
        String id = UUID.randomUUID().toString().substring(0, 8);
        return dynasty + "-" + year + "-" + id + ".png";
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
