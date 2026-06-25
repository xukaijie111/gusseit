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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private final RoundRepository roundRepository;
    private final GeocodeClient geocodeClient;

    public GameService(RoundRepository roundRepository, GeocodeClient geocodeClient) {
        this.roundRepository = roundRepository;
        this.geocodeClient = geocodeClient;
    }

    public Map<String, String> lookupCity(double latitude, double longitude) {
        CityPoint city = geocodeClient.reverseCity(latitude, longitude);
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("city", city.getName());
        return result;
    }

    public List<GameRoundView> createSession(String dynasty, String era, int count) {
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

        CityPoint guessCity = geocodeClient.reverseCity(request.getLatitude(), request.getLongitude());
        CityPoint answerCity = geocodeClient.resolveModernCity(round.getModernPlace());
        if (answerCity == null && round.getGeoQuery() != null && !round.getGeoQuery().trim().isEmpty()) {
            answerCity = geocodeClient.resolveCity(round.getGeoQuery(), null);
        }

        int dynastyScore = ScoreCalculator.dynastyScore(
                TimelineConstants.dynastyAt(request.getYearAd()),
                round.getDynasty()
        );

        String guessCityName = guessCity.getName();
        String answerCityName = answerCity != null
                ? answerCity.getName()
                : GeocodeClient.formatCityLabel(round.getModernPlace());

        boolean cityMatch = GeocodeClient.sameCity(guessCityName, answerCityName)
                || GeocodeClient.sameCity(guessCityName, round.getModernPlace());

        int geoScore = 0;
        double distanceKm = 0;
        if (cityMatch) {
            distanceKm = 0;
            geoScore = 100;
        } else if (answerCity != null) {
            distanceKm = ScoreCalculator.haversineKm(
                    guessCity.getLatitude(), guessCity.getLongitude(),
                    answerCity.getLatitude(), answerCity.getLongitude()
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

        Double answerLat = answerCity != null ? answerCity.getLatitude() : null;
        Double answerLng = answerCity != null ? answerCity.getLongitude() : null;

        return GuessResultView.create(
                dynastyScore,
                geoScore,
                distanceKm,
                TimelineConstants.dynastyAt(request.getYearAd()),
                round.getDynasty(),
                request.getYearAd(),
                round.getYearAd(),
                guessCityName,
                answerCityName,
                guessCity.getLatitude(),
                guessCity.getLongitude(),
                answerLat,
                answerLng,
                answer
        );
    }
}
