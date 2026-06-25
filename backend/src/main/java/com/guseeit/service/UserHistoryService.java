package com.guseeit.service;

import com.guseeit.domain.Round;
import com.guseeit.domain.UserHistory;
import com.guseeit.dto.GuessResultView;
import com.guseeit.dto.UserHistoryView;
import com.guseeit.repository.RoundRepository;
import com.guseeit.repository.UserHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserHistoryService {

    private final UserHistoryRepository historyRepository;
    private final RoundRepository roundRepository;

    public UserHistoryService(UserHistoryRepository historyRepository, RoundRepository roundRepository) {
        this.historyRepository = historyRepository;
        this.roundRepository = roundRepository;
    }

    public List<String> answeredRoundIds(Long userId) {
        return historyRepository.findRoundIdsByUserId(userId);
    }

    public void save(Long userId, GuessResultView result, String roundId, String imageUrl) {
        UserHistory h = new UserHistory();
        h.setUserId(userId);
        h.setRoundId(roundId);
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
        h.setImageUrl(imageUrl);

        GuessResultView.AnswerView ans = result.getAnswer();
        if (ans != null) {
            h.setLocationName(ans.getLocationName());
            h.setModernPlace(ans.getModernPlace());
        }

        historyRepository.save(h);
    }

    public int count(Long userId) {
        return historyRepository.countByUserId(userId);
    }

    public List<UserHistoryView> list(Long userId, int offset, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        int page = offset / size;
        List<UserHistory> rows = historyRepository.findByUserIdOrderByAnsweredAtDesc(
                userId, PageRequest.of(page, size));

        List<UserHistoryView> views = new ArrayList<>();
        for (UserHistory h : rows) {
            UserHistoryView v = new UserHistoryView();
            v.setId(h.getId());
            v.setRoundId(h.getRoundId());
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

            Optional<Round> round = roundRepository.findById(h.getRoundId());
            if (round.isPresent()) {
                Round r = round.get();
                v.setHistoricalCity(r.getHistoricalCity());
                v.setTimeLabel(r.getTimeLabel());
                v.setKnowledgeSummary(r.getKnowledgeSummary());
                v.setAnecdoteTitle(r.getSceneType());
            }

            views.add(v);
        }
        return views;
    }
}
