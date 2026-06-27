package com.guseeit.service;

import com.guseeit.client.ArkImageClient;
import com.guseeit.client.GeocodeClient;
import com.guseeit.client.OssService;
import com.guseeit.client.QwenClient;
import com.guseeit.config.GuseeitProperties;
import com.guseeit.domain.AnecdoteData;
import com.guseeit.domain.AnecdoteDraft;
import com.guseeit.domain.AnecdoteImage;
import com.guseeit.dto.AnecdoteItemDto;
import com.guseeit.repository.AnecdoteDataRepository;
import com.guseeit.repository.AnecdoteDraftRepository;
import com.guseeit.repository.AnecdoteImageRepository;
import com.guseeit.support.DynastyConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AnecdoteService {

    private final AnecdoteDataRepository anecdoteDataRepository;
    private final AnecdoteDraftRepository anecdoteDraftRepository;
    private final AnecdoteImageRepository anecdoteImageRepository;
    private final GuseeitProperties properties;
    private final QwenClient qwenClient;
    private final GeocodeClient geocodeClient;
    private final ArkImageClient arkImageClient;
    private final OssService ossService;

    public AnecdoteService(
            AnecdoteDataRepository anecdoteDataRepository,
            AnecdoteDraftRepository anecdoteDraftRepository,
            AnecdoteImageRepository anecdoteImageRepository,
            GuseeitProperties properties,
            QwenClient qwenClient,
            GeocodeClient geocodeClient,
            ArkImageClient arkImageClient,
            OssService ossService
    ) {
        this.anecdoteDataRepository = anecdoteDataRepository;
        this.anecdoteDraftRepository = anecdoteDraftRepository;
        this.anecdoteImageRepository = anecdoteImageRepository;
        this.properties = properties;
        this.qwenClient = qwenClient;
        this.geocodeClient = geocodeClient;
        this.arkImageClient = arkImageClient;
        this.ossService = ossService;
    }

    private static AnecdoteItemDto fromDraft(AnecdoteDraft draft) {
        AnecdoteItemDto dto = new AnecdoteItemDto();
        dto.setId(draft.getId());
        dto.setDynastyId(draft.getDynastyId());
        dto.setDynastyName(DynastyConstants.toName(draft.getDynastyId()));
        dto.setAnecdoteName(draft.getAnecdoteName());
        dto.setSummary(draft.getSummary());
        dto.setHistoricalPlace(draft.getHistoricalPlace());
        dto.setModernLocation(draft.getModernLocation());
        dto.setModernCity(draft.getModernCity());
        dto.setLatitude(draft.getLatitude());
        dto.setLongitude(draft.getLongitude());
        return dto;
    }

    private static AnecdoteItemDto fromData(AnecdoteData data) {
        AnecdoteItemDto dto = new AnecdoteItemDto();
        dto.setId(data.getId());
        dto.setDynastyId(data.getDynastyId());
        dto.setDynastyName(DynastyConstants.toName(data.getDynastyId()));
        dto.setAnecdoteName(data.getAnecdoteName());
        dto.setSummary(data.getSummary());
        dto.setHistoricalPlace(data.getHistoricalPlace());
        dto.setModernLocation(data.getModernLocation());
        dto.setModernCity(data.getModernCity());
        dto.setLatitude(data.getLatitude());
        dto.setLongitude(data.getLongitude());
        return dto;
    }

    // ==================== 生成 → 草稿表 ====================

    public static final class DraftGenResult {
        private final List<AnecdoteItemDto> saved;
        private final List<String> errors;

        public DraftGenResult(List<AnecdoteItemDto> saved, List<String> errors) {
            this.saved = saved;
            this.errors = errors;
        }
        public List<AnecdoteItemDto> getSaved() { return saved; }
        public List<String> getErrors() { return errors; }
    }

    @Transactional
    public DraftGenResult generateAndDraft(Integer dynastyId, int count) {
        if (count < 1 || count > 20) throw new IllegalArgumentException("数量须为 1–20");
        String dynastyName = DynastyConstants.toName(dynastyId);
        if (dynastyName == null) throw new IllegalArgumentException("未知朝代");

        // 去重
        Set<String> existingNames = new HashSet<>();
        for (AnecdoteData data : anecdoteDataRepository.findAll()) {
            if (data.getAnecdoteName() != null) existingNames.add(data.getAnecdoteName().trim());
        }
        for (AnecdoteDraft draft : anecdoteDraftRepository.findAll()) {
            if (draft.getAnecdoteName() != null) existingNames.add(draft.getAnecdoteName().trim());
        }

        QwenClient.AnecdoteGenerationResult genResult = qwenClient.generateAnecdotes(dynastyName, count, existingNames);
        List<AnecdoteItemDto> saved = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        errors.addAll(genResult.getErrors());

        for (AnecdoteItemDto dto : genResult.getItems()) {
            GeocodeClient.CityPoint point = geocodeClient.resolveCityCenter(dto.getModernCity());
            if (point == null) {
                String shortCity = dto.getModernCity();
                if (shortCity != null && shortCity.contains("省"))
                    shortCity = shortCity.substring(shortCity.indexOf("省") + 1);
                point = geocodeClient.resolveCityCenter(shortCity);
            }
            if (point == null) {
                errors.add("「" + dto.getAnecdoteName() + "」地理位置「" + dto.getModernCity() + "」无法解析，已跳过");
                continue;
            }

            AnecdoteDraft draft = new AnecdoteDraft();
            draft.setDynastyId(dynastyId);
            draft.setAnecdoteName(dto.getAnecdoteName());
            draft.setSummary(dto.getSummary());
            draft.setHistoricalPlace(dto.getHistoricalPlace());
            draft.setModernLocation(dto.getModernLocation());
            draft.setModernCity(dto.getModernCity());
            draft.setLatitude(point.getLatitude());
            draft.setLongitude(point.getLongitude());
            draft.setModelName(properties.getQwen().getModel());
            anecdoteDraftRepository.save(draft);

            dto.setDynastyId(dynastyId);
            dto.setDynastyName(dynastyName);
            dto.setLatitude(point.getLatitude());
            dto.setLongitude(point.getLongitude());
            saved.add(dto);
        }
        return new DraftGenResult(saved, errors);
    }

    // ==================== 草稿管理 ====================

    public Page<AnecdoteDraft> listDrafts(Integer dynastyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (dynastyId != null) return anecdoteDraftRepository.findByDynastyId(dynastyId, pageable);
        return anecdoteDraftRepository.findAll(pageable);
    }

    @Transactional
    public int approveDrafts(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            AnecdoteDraft draft = anecdoteDraftRepository.findById(id).orElse(null);
            if (draft == null) continue;
            // 去重
            if (!anecdoteDataRepository.findByAnecdoteName(draft.getAnecdoteName()).isEmpty()) {
                anecdoteDraftRepository.delete(draft);
                continue;
            }
            AnecdoteData data = new AnecdoteData();
            data.setDynastyId(draft.getDynastyId());
            data.setAnecdoteName(draft.getAnecdoteName());
            data.setSummary(draft.getSummary());
            data.setHistoricalPlace(draft.getHistoricalPlace());
            data.setModernLocation(draft.getModernLocation());
            data.setModernCity(draft.getModernCity());
            data.setLatitude(draft.getLatitude());
            data.setLongitude(draft.getLongitude());
            data.setModelName(draft.getModelName());
            anecdoteDataRepository.save(data);
            anecdoteDraftRepository.delete(draft);
            count++;
        }
        return count;
    }

    @Transactional
    public int rejectDrafts(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            if (anecdoteDraftRepository.existsById(id)) { anecdoteDraftRepository.deleteById(id); count++; }
        }
        return count;
    }

    // ==================== 典故库 ====================

    public Page<AnecdoteData> list(Integer dynastyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (dynastyId != null) return anecdoteDataRepository.findByDynastyId(dynastyId, pageable);
        return anecdoteDataRepository.findAll(pageable);
    }

    // ==================== 图片表 ====================

    public Page<AnecdoteImage> listImages(Integer dynastyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (dynastyId != null) return anecdoteImageRepository.findByDynastyId(dynastyId, pageable);
        return anecdoteImageRepository.findAll(pageable);
    }

    @Transactional
    public int deleteImages(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            AnecdoteImage image = anecdoteImageRepository.findById(id).orElse(null);
            if (image == null) continue;
            ossService.delete(image.getOssObjectKey());
            anecdoteImageRepository.delete(image);
            count++;
        }
        return count;
    }

    @Transactional
    public int[] generateImages(List<Long> ids, ProgressCallback onProgress) {
        int success = 0, fail = 0;
        for (int i = 0; i < ids.size(); i++) {
            AnecdoteData anecdote = anecdoteDataRepository.findById(ids.get(i)).orElse(null);
            if (anecdote == null) { fail++; continue; }
            String name = anecdote.getAnecdoteName();
            onProgress.onProgress("生成: " + name);
            try {
                String prompt = "生成一张典故《" + name + "》的图片，图里要有明显的一条线索能看出这是什么典故，要求竖屏构图，高清，写实古风摄影，3D，无现代元素。";
                byte[] imageBytes = arkImageClient.generateImage(prompt);
                OssService.UploadResult upload = ossService.upload(imageBytes, name);
                AnecdoteImage image = new AnecdoteImage();
                image.setDynastyId(anecdote.getDynastyId());
                image.setAnecdoteName(anecdote.getAnecdoteName());
                image.setSummary(anecdote.getSummary());
                image.setHistoricalPlace(anecdote.getHistoricalPlace());
                image.setModernLocation(anecdote.getModernLocation());
                image.setModernCity(anecdote.getModernCity());
                image.setLatitude(anecdote.getLatitude());
                image.setLongitude(anecdote.getLongitude());
                image.setImageUrl(upload.getImageUrl());
                image.setOssObjectKey(upload.getObjectKey());
                image.setImageSize(arkImageClient.getImageSize());
                image.setPrompt(prompt);
                image.setModelName(properties.getArk().getModel());
                anecdoteImageRepository.save(image);
                success++;
            } catch (Exception e) {
                fail++;
                onProgress.onProgress(name + " 失败: " + e.getMessage());
            }
            if (i < ids.size() - 1) {
                try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
        return new int[]{success, fail};
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(String message);
    }
}
