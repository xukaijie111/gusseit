package com.guseeit.service;

import com.guseeit.client.GeocodeClient;
import com.guseeit.client.GeocodeClient.CityPoint;
import com.guseeit.domain.AnecdoteImage;
import com.guseeit.dto.GameRoundView;
import com.guseeit.dto.GuessRequest;
import com.guseeit.dto.GuessResultView;
import com.guseeit.repository.AnecdoteImageRepository;
import com.guseeit.support.DynastyConstants;
import com.guseeit.support.EraConstants;
import com.guseeit.support.KnowledgeSummaryHelper;
import com.guseeit.support.ScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final AnecdoteImageRepository imageRepository;
    private final GeocodeClient geocodeClient;
    private final UserService userService;
    private final UserHistoryService historyService;

    public GameService(AnecdoteImageRepository imageRepository, GeocodeClient geocodeClient,
                       UserService userService, UserHistoryService historyService) {
        this.imageRepository = imageRepository;
        this.geocodeClient = geocodeClient;
        this.userService = userService;
        this.historyService = historyService;
    }

    public Map<String, String> lookupCity(double lat, double lng) {
        CityPoint city = geocodeClient.reverseCity(lat, lng);
        Map<String, String> r = new LinkedHashMap<>();
        r.put("city", city.getName());
        return r;
    }

    public Map<String, Object> lookupCityCenter(String city) {
        CityPoint point = geocodeClient.resolveCityCenter(city);
        Map<String, Object> r = new LinkedHashMap<>();
        if (point == null) {
            r.put("city", city);
            r.put("latitude", null);
            r.put("longitude", null);
            return r;
        }
        r.put("city", point.getName());
        r.put("latitude", point.getLatitude());
        r.put("longitude", point.getLongitude());
        return r;
    }

    public List<GameRoundView> createSession(Integer dynastyId, String era, int count, String token) {
        int size = Math.min(Math.max(count, 1), 10);
        Pageable pageable = PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<AnecdoteImage> pool;
        List<Integer> eraIds = EraConstants.dynastyIdsFor(era);
        if (eraIds != null) {
            pool = new ArrayList<>(imageRepository.findByDynastyIdIn(eraIds, pageable).getContent());
        } else if (dynastyId != null) {
            pool = new ArrayList<>(imageRepository.findByDynastyId(dynastyId, pageable).getContent());
        } else {
            pool = new ArrayList<>(imageRepository.findAll(pageable).getContent());
        }

        pool.removeIf(r -> r.getImageUrl() == null || r.getImageUrl().trim().isEmpty());

        Long userId = userService.resolveUserId(token);
        List<AnecdoteImage> selected = selectPool(pool, userId, size);

        if (selected.isEmpty()) {
            throw new IllegalStateException("暂无可用题目，请先在管理端生成图片");
        }

        List<GameRoundView> views = new ArrayList<>();
        int total = selected.size();
        for (int i = 0; i < total; i++) {
            AnecdoteImage img = selected.get(i);
            views.add(GameRoundView.of(img.getId(), DynastyConstants.toName(img.getDynastyId()), img.getImageUrl(), i + 1, total));
        }
        return views;
    }

    /** 优先未答过的题；不足 count 时再补已答过的。 */
    private List<AnecdoteImage> selectPool(List<AnecdoteImage> pool, Long userId, int count) {
        if (pool.isEmpty()) return Collections.emptyList();

        if (userId == null) {
            List<AnecdoteImage> copy = new ArrayList<>(pool);
            Collections.shuffle(copy);
            return copy.subList(0, Math.min(count, copy.size()));
        }

        Set<Long> answered = new HashSet<>(historyService.answeredImageIds(userId));
        List<AnecdoteImage> fresh = new ArrayList<>();
        List<AnecdoteImage> done = new ArrayList<>();
        for (AnecdoteImage img : pool) {
            if (answered.contains(img.getId())) {
                done.add(img);
            } else {
                fresh.add(img);
            }
        }

        Collections.shuffle(fresh);
        Collections.shuffle(done);

        List<AnecdoteImage> selected = new ArrayList<>();
        for (AnecdoteImage img : fresh) {
            if (selected.size() >= count) break;
            selected.add(img);
        }
        if (selected.size() < count) {
            for (AnecdoteImage img : done) {
                if (selected.size() >= count) break;
                selected.add(img);
            }
        }
        return selected;
    }

    public GuessResultView submitGuess(GuessRequest request) {
        AnecdoteImage image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        CityPoint guessReverse = geocodeClient.reverseCity(request.getLatitude(), request.getLongitude());
        String guessCityName = guessReverse.getName();
        CityPoint guessCenter = geocodeClient.resolveCityCenter(guessCityName);
        if (guessCenter == null) guessCenter = guessReverse;

        String answerCityName = image.getModernCity();
        CityPoint answerCenter = geocodeClient.resolveCityCenter(answerCityName);

        String guessDynastyName = DynastyConstants.toName(request.getDynastyId());
        String answerDynastyName = DynastyConstants.toName(image.getDynastyId());

        int dynastyScore = ScoreCalculator.dynastyScoreById(request.getDynastyId(), image.getDynastyId());

        boolean cityMatch = GeocodeClient.sameCity(guessCityName, answerCityName)
                || GeocodeClient.cityContains(guessCityName, answerCityName);

        int geoScore = cityMatch ? 100 : 0;
        double distanceKm = 0;
        if (!cityMatch && guessCenter != null && answerCenter != null) {
            distanceKm = ScoreCalculator.haversineKm(
                    guessCenter.getLatitude(), guessCenter.getLongitude(),
                    answerCenter.getLatitude(), answerCenter.getLongitude());
        }

        GuessResultView.AnswerView answer = new GuessResultView.AnswerView();
        answer.setDynasty(answerDynastyName);
        answer.setModernPlace(image.getModernCity());
        answer.setTimeLabel(answerDynastyName);
        answer.setKnowledgeSummary(KnowledgeSummaryHelper.resolve(image));
        answer.setAnecdoteTitle(KnowledgeSummaryHelper.anecdoteTitle(image));
        answer.setHistoricalCityName(image.getHistoricalPlace());
        answer.setBaikeUrl(KnowledgeSummaryHelper.baikeSearchUrl(image));

        GuessResultView result = GuessResultView.create(
                dynastyScore, geoScore, distanceKm,
                guessDynastyName, answerDynastyName,
                null, null,
                guessCityName, answerCityName,
                guessCenter.getLatitude(), guessCenter.getLongitude(),
                image.getLatitude(), image.getLongitude(),
                answer);

        Long userId = userService.resolveUserId(request.getToken());
        if (userId != null) {
            try {
                historyService.save(userId, result, image);
            } catch (Exception e) {
                log.warn("保存答题历史失败 userId={} imageId={}: {}", userId, image.getId(), e.getMessage());
            }
        }
        return result;
    }
}
