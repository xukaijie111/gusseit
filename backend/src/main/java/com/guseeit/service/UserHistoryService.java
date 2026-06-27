package com.guseeit.service;

import com.guseeit.domain.AnecdoteImage;
import com.guseeit.domain.UserHistory;
import com.guseeit.dto.GuessResultView;
import com.guseeit.dto.UserHistoryView;
import com.guseeit.repository.AnecdoteImageRepository;
import com.guseeit.repository.UserHistoryRepository;
import com.guseeit.support.DynastyConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserHistoryService {

    private final UserHistoryRepository historyRepository;
    private final AnecdoteImageRepository imageRepository;

    public UserHistoryService(UserHistoryRepository historyRepository, AnecdoteImageRepository imageRepository) {
        this.historyRepository = historyRepository;
        this.imageRepository = imageRepository;
    }

    public List<Long> answeredImageIds(Long userId) {
        return historyRepository.findImageIdsByUserId(userId);
    }

    public void save(Long userId, GuessResultView result, AnecdoteImage image) {
        UserHistory h = new UserHistory();
        h.setUserId(userId);
        h.setImageId(image.getId());
        h.setRoundId(String.valueOf(image.getId()));
        h.setGuessCity(result.getGuessCity());
        h.setGuessLat(result.getGuessLatitude());
        h.setGuessLng(result.getGuessLongitude());
        h.setGuessYear(result.getGuessYearAd());
        h.setGuessDynasty(result.getGuessDynasty());
        h.setAnswerCity(result.getAnswerCity());
        h.setAnswerLat(result.getAnswerLatitude());
        h.setAnswerLng(result.getAnswerLongitude());
        h.setAnswerYear(result.getAnswerYearAd());
        h.setAnswerDynasty(result.getAnswerDynasty());
        h.setTotalScore(result.getTotalScore());
        h.setDynastyScore(result.getDynastyScore());
        h.setGeoScore(result.getGeoScore());
        h.setDistanceKm(result.getDistanceKm());
        h.setImageUrl(image.getImageUrl());
        h.setLocationName(image.getHistoricalPlace());
        h.setModernPlace(image.getModernCity());
        h.setAnecdoteName(image.getAnecdoteName());
        h.setKnowledgeSummary(image.getSummary());
        historyRepository.save(h);
    }

    public int count(Long userId) {
        return historyRepository.countByUserId(userId);
    }

    public List<UserHistoryView> list(Long userId, int offset, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        List<UserHistory> rows = historyRepository.findByUserIdOrderByAnsweredAtDesc(userId, PageRequest.of(offset / size, size));
        List<UserHistoryView> views = new ArrayList<>();
        for (UserHistory h : rows) {
            UserHistoryView v = new UserHistoryView();
            v.setId(h.getId());
            v.setRoundId(String.valueOf(h.getImageId()));
            v.setGuessCity(h.getGuessCity());
            v.setGuessLat(h.getGuessLat());
            v.setGuessLng(h.getGuessLng());
            v.setGuessYear(h.getGuessYear());
            v.setGuessDynasty(h.getGuessDynasty());
            v.setAnswerCity(h.getAnswerCity());
            v.setAnswerLat(h.getAnswerLat());
            v.setAnswerLng(h.getAnswerLng());
            v.setAnswerYear(h.getAnswerYear());
            v.setAnswerDynasty(h.getAnswerDynasty());
            v.setTotalScore(h.getTotalScore());
            v.setDynastyScore(h.getDynastyScore());
            v.setGeoScore(h.getGeoScore());
            v.setDistanceKm(h.getDistanceKm());
            v.setImageUrl(h.getImageUrl());
            v.setLocationName(h.getLocationName());
            v.setModernPlace(h.getModernPlace());
            v.setAnsweredAt(h.getAnsweredAt().toString());
            v.setAnecdoteTitle(h.getAnecdoteName());
            v.setKnowledgeSummary(h.getKnowledgeSummary());

            if (h.getImageId() != null) {
                Optional<AnecdoteImage> img = imageRepository.findById(h.getImageId());
                if (img.isPresent()) {
                    v.setHistoricalCity(img.get().getHistoricalPlace());
                    v.setTimeLabel(DynastyConstants.toName(img.get().getDynastyId()));
                }
            }
            views.add(v);
        }
        return views;
    }
}
