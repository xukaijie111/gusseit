package com.guseeit.service;

import com.guseeit.client.GeocodeClient;
import com.guseeit.client.GeocodeClient.CityPoint;
import com.guseeit.domain.Round;
import com.guseeit.domain.RoundStatus;
import com.guseeit.dto.GameRoundView;
import com.guseeit.dto.GuessRequest;
import com.guseeit.dto.GuessResultView;
import com.guseeit.repository.RoundRepository;
import com.guseeit.support.EraConstants;
import com.guseeit.support.KnowledgeSummaryHelper;
import com.guseeit.support.ScoreCalculator;
import com.guseeit.support.TimelineConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GameService {

    private final RoundRepository roundRepository;
    private final GeocodeClient geocodeClient;
    private final UserService userService;
    private final UserHistoryService historyService;

    public GameService(RoundRepository roundRepository, GeocodeClient geocodeClient,
                       UserService userService, UserHistoryService historyService) {
        this.roundRepository = roundRepository;
        this.geocodeClient = geocodeClient;
        this.userService = userService;
        this.historyService = historyService;
    }

    public Map<String, String> lookupCity(double latitude, double longitude) {
        CityPoint city = geocodeClient.reverseCity(latitude, longitude);
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("city", city.getName());
        return result;
    }

    public Map<String, Object> lookupCityCenter(String city) {
        CityPoint point = geocodeClient.resolveCityCenter(city);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (point == null) {
            result.put("city", city);
            result.put("latitude", null);
            result.put("longitude", null);
            return result;
        }
        result.put("city", point.getName());
        result.put("latitude", point.getLatitude());
        result.put("longitude", point.getLongitude());
        return result;
    }

    public List<GameRoundView> createSession(String dynasty, String era, int count, String token) {
        int size = Math.min(Math.max(count, 1), 10);
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Round> pool;
        List<String> eraDynasties = EraConstants.dynastiesFor(era);
        if (eraDynasties != null) {
            pool = new ArrayList<Round>(
                    roundRepository.findByDynastyInAndStatus(eraDynasties, RoundStatus.generated, pageable).getContent()
            );
        } else if (dynasty != null && !dynasty.trim().isEmpty()) {
            pool = new ArrayList<Round>(
                    roundRepository.findByDynastyAndStatus(dynasty, RoundStatus.generated, pageable).getContent()
            );
        } else {
            pool = new ArrayList<Round>(
                    roundRepository.findByStatus(RoundStatus.generated, pageable).getContent()
            );
        }

        pool.removeIf(r -> r.getImageUrl() == null || r.getImageUrl().trim().isEmpty());

        // 排除用户已答过的题目
        Long userId = userService.resolveUserId(token);
        if (userId != null) {
            List<String> answered = historyService.answeredRoundIds(userId);
            if (!answered.isEmpty()) {
                Set<String> answeredSet = new HashSet<>(answered);
                pool.removeIf(r -> answeredSet.contains(r.getId()));
            }
        }

        if (pool.isEmpty()) {
            throw new IllegalStateException("暂无可用题目，请先在管理端生成图片");
        }

        Collections.shuffle(pool);
        int take = Math.min(size, pool.size());
        List<GameRoundView> views = new ArrayList<GameRoundView>();
        for (int i = 0; i < take; i++) {
            Round round = pool.get(i);
            views.add(GameRoundView.of(round.getId(), round.getDynasty(), round.getImageUrl(), i + 1, take));
        }
        return views;
    }

    public GuessResultView submitGuess(GuessRequest request) {
        Round round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        if (round.getStatus() != RoundStatus.generated) {
            throw new IllegalArgumentException("该题目不可作答");
        }

        CityPoint guessReverse = geocodeClient.reverseCity(request.getLatitude(), request.getLongitude());
        String guessCityName = guessReverse.getName();

        CityPoint guessCenter = geocodeClient.resolveCityCenter(guessCityName);
        if (guessCenter == null) {
            guessCenter = guessReverse;
        }

        CityPoint answerCenter = resolveAnswerCenter(round);
        String answerCityName = answerCenter != null
                ? answerCenter.getName()
                : GeocodeClient.formatCityLabel(round.getModernPlace());

        int dynastyScore = ScoreCalculator.dynastyScore(
                TimelineConstants.dynastyAt(request.getYearAd()),
                round.getDynasty()
        );

        boolean cityMatch = GeocodeClient.sameCity(guessCityName, answerCityName)
                || GeocodeClient.sameCity(guessCityName, round.getModernPlace());

        int geoScore = 0;
        double distanceKm = 0;
        if (cityMatch) {
            distanceKm = 0;
            geoScore = 100;
        } else if (guessCenter != null && answerCenter != null) {
            distanceKm = ScoreCalculator.haversineKm(
                    guessCenter.getLatitude(), guessCenter.getLongitude(),
                    answerCenter.getLatitude(), answerCenter.getLongitude()
            );
            geoScore = ScoreCalculator.geoScore(distanceKm);
        }

        GuessResultView.AnswerView answer = new GuessResultView.AnswerView();
        answer.setDynasty(round.getDynasty());
        answer.setLocationName(round.getLocationName());
        answer.setModernPlace(round.getModernPlace());
        answer.setTimeLabel(round.getTimeLabel());
        answer.setYearAd(round.getYearAd());
        answer.setKnowledgeSummary(KnowledgeSummaryHelper.resolve(round));
        answer.setAnecdoteTitle(KnowledgeSummaryHelper.anecdoteTitle(round));
        answer.setBaikeUrl(KnowledgeSummaryHelper.baikeSearchUrl(round));
        answer.setHistoricalCityName(KnowledgeSummaryHelper.historicalCityName(round));

        Double answerLat = answerCenter != null ? answerCenter.getLatitude() : null;
        Double answerLng = answerCenter != null ? answerCenter.getLongitude() : null;

        GuessResultView result = GuessResultView.create(
                dynastyScore,
                geoScore,
                distanceKm,
                TimelineConstants.dynastyAt(request.getYearAd()),
                round.getDynasty(),
                request.getYearAd(),
                round.getYearAd(),
                guessCityName,
                answerCityName,
                guessCenter.getLatitude(),
                guessCenter.getLongitude(),
                answerLat,
                answerLng,
                answer
        );

        // 已登录用户保存答题记录
        Long userId = userService.resolveUserId(request.getToken());
        if (userId != null) {
            try {
                historyService.save(userId, result, round.getId(), round.getImageUrl());
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    private CityPoint resolveAnswerCenter(Round round) {
        CityPoint center = geocodeClient.resolveCityCenter(round.getModernPlace());
        if (center != null) {
            return center;
        }
        if (round.getGeoQuery() != null && !round.getGeoQuery().trim().isEmpty()) {
            center = geocodeClient.resolveCityCenter(round.getGeoQuery());
            if (center != null) {
                return center;
            }
        }
        if (round.getHistoricalCity() != null && !round.getHistoricalCity().trim().isEmpty()) {
            center = geocodeClient.resolveCityCenter(round.getHistoricalCity() + "市");
            if (center != null) {
                return center;
            }
        }
        return geocodeClient.resolveCity(round.getGeoQuery(), round.getModernPlace());
    }
}
