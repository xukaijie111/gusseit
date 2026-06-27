package com.guseeit.controller;

import com.guseeit.dto.GameRoundView;
import com.guseeit.dto.GuessRequest;
import com.guseeit.dto.GuessResultView;
import com.guseeit.service.GameService;
import com.guseeit.support.DynastyConstants;
import com.guseeit.support.EraConstants;
import com.guseeit.support.TimelineConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/dynasties")
    public Map<String, Object> dynasties() {
        List<Map<String, Object>> dynastyList = new ArrayList<>();
        for (TimelineConstants.Period p : TimelineConstants.getAllPeriods()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", p.getKey());
            m.put("ruler", p.getKey());       // 尺子刻字与 key 一致
            m.put("title", p.getTitle());
            m.put("start", p.getStart());
            m.put("end", p.getEnd());
            Integer id = DynastyConstants.toId(p.getKey());
            m.put("id", id);
            m.put("name", p.getKey());
            m.put("slug", DynastyConstants.slug(id));
            dynastyList.add(m);
        }

        List<Map<String, Object>> eraList = new ArrayList<>();
        for (EraConstants.Era e : EraConstants.getAll()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", e.getKey());
            m.put("ruler", e.getRuler());
            m.put("title", e.getTitle());
            m.put("subtitle", e.getSubtitle());
            m.put("dynastyIds", e.getDynastyIds());
            eraList.add(m);
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dynasties", dynastyList);
        r.put("eras", eraList);
        return r;
    }

    @GetMapping("/session")
    public Map<String, Object> session(
            @RequestParam(required = false) Integer dynastyId,
            @RequestParam(required = false) String era,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(required = false) String token) {
        List<GameRoundView> rounds = gameService.createSession(dynastyId, era, count, token);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("rounds", rounds);
        r.put("total", rounds.size());
        return r;
    }

    @PostMapping("/guess")
    public GuessResultView guess(@Valid @RequestBody GuessRequest request) {
        return gameService.submitGuess(request);
    }

    @GetMapping("/reverse-city")
    public Map<String, String> reverseCity(@RequestParam double latitude, @RequestParam double longitude) {
        return gameService.lookupCity(latitude, longitude);
    }

    @GetMapping("/city-center")
    public Map<String, Object> cityCenter(@RequestParam String city) {
        return gameService.lookupCityCenter(city);
    }
}
