package com.guseeit.service;

import com.guseeit.client.ArkImageClient;
import com.guseeit.client.GeocodeClient;
import com.guseeit.client.OssService;
import com.guseeit.client.QwenClient;
import com.guseeit.domain.Round;
import com.guseeit.domain.RoundStatus;
import com.guseeit.dto.RoundPromptDto;
import com.guseeit.repository.RoundRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GenerationService {

    private final RoundRepository roundRepository;
    private final QwenClient qwenClient;
    private final ArkImageClient arkImageClient;
    private final OssService ossService;
    private final GeocodeClient geocodeClient;

    public GenerationService(
            RoundRepository roundRepository,
            QwenClient qwenClient,
            ArkImageClient arkImageClient,
            OssService ossService,
            GeocodeClient geocodeClient
    ) {
        this.roundRepository = roundRepository;
        this.qwenClient = qwenClient;
        this.arkImageClient = arkImageClient;
        this.ossService = ossService;
        this.geocodeClient = geocodeClient;
    }

    public static String dedupKey(String dynasty, String locationName, Integer yearAd) {
        return dynasty + "|" + locationName.trim() + "|" + yearAd;
    }

    public GenerationResult runGeneration(String dynasty, int count, ProgressCallback onProgress) {
        List<Round> existingRows = roundRepository.findByDynasties(Collections.singletonList(dynasty));
        Set<String> existingKeys = new HashSet<String>();
        for (Round r : existingRows) {
            existingKeys.add(dedupKey(r.getDynasty(), r.getLocationName(), r.getYearAd()));
        }

        List<RoundPromptDto> rounds = new ArrayList<RoundPromptDto>();
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            onProgress.onProgress("千问生成提示词 (" + attempt + "/" + maxAttempts + ")...");

            List<RoundPromptDto> batch = qwenClient.generatePrompts(dynasty, count, existingRows);
            List<RoundPromptDto> fresh = dedupe(batch, existingKeys);
            rounds = fresh;

            if (rounds.size() >= count) {
                rounds = rounds.subList(0, count);
                break;
            }

            onProgress.onProgress("去重后仅 " + fresh.size() + "/" + count + " 条，"
                    + (attempt < maxAttempts ? "重试..." : "继续..."));

            if (attempt == maxAttempts && rounds.isEmpty()) {
                throw new IllegalStateException("千问生成的题目均与题库重复，请换朝代或减少数量");
            }
        }

        int successCount = 0;
        int failCount = 0;
        int geocodeFailCount = 0;

        for (int i = 0; i < rounds.size(); i++) {
            RoundPromptDto meta = rounds.get(i);
            onProgress.onProgress(meta.getTimeLabel() + " · " + meta.getLocationName());

            // 先验证高德地图能不能拿到经纬度
            GeocodeClient.CityPoint cityPoint = geocodeClient.resolveCityCenter(meta.getModernPlace());
            if (cityPoint == null) {
                onProgress.onProgress(meta.getTimeLabel() + " 地理编码失败，跳过");
                geocodeFailCount++;
                continue;
            }

            Round round = meta.toEntity();
            round.setLatitude(cityPoint.getLatitude());
            round.setLongitude(cityPoint.getLongitude());
            round.setStatus(RoundStatus.pending);
            round = roundRepository.save(round);

            try {
                byte[] imageBytes = arkImageClient.generateImage(meta.getPrompt());
                OssService.UploadResult upload = ossService.upload(imageBytes, meta);

                round.setImageUrl(upload.getImageUrl());
                round.setOssObjectKey(upload.getObjectKey());
                round.setImageSize(arkImageClient.getImageSize());
                round.setStatus(RoundStatus.generated);
                round.setErrorMessage(null);
                roundRepository.save(round);

                successCount++;
                existingKeys.add(dedupKey(meta.getDynasty(), meta.getLocationName(), meta.getYearAd()));
            } catch (Exception e) {
                round.setStatus(RoundStatus.failed);
                round.setErrorMessage(e.getMessage());
                roundRepository.save(round);
                failCount++;
            }

            if (i < rounds.size() - 1) {
                sleep(1500);
            }
        }

        return new GenerationResult(successCount, failCount, geocodeFailCount, rounds.size());
    }

    private List<RoundPromptDto> dedupe(List<RoundPromptDto> rounds, Set<String> existingKeys) {
        Set<String> seen = new HashSet<String>(existingKeys);
        List<RoundPromptDto> out = new ArrayList<RoundPromptDto>();
        for (RoundPromptDto r : rounds) {
            String key = dedupKey(r.getDynasty(), r.getLocationName(), r.getYearAd());
            if (seen.contains(key)) continue;
            seen.add(key);
            out.add(r);
        }
        return out;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public interface ProgressCallback {
        void onProgress(String message);
    }

    public static class GenerationResult {
        private final int successCount;
        private final int failCount;
        private final int geocodeFailCount;
        private final int total;

        public GenerationResult(int successCount, int failCount, int geocodeFailCount, int total) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.geocodeFailCount = geocodeFailCount;
            this.total = total;
        }

        public int getSuccessCount() { return successCount; }
        public int getFailCount() { return failCount; }
        public int getGeocodeFailCount() { return geocodeFailCount; }
        public int getTotal() { return total; }
    }
}
