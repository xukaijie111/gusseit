package com.guseeit.controller;

import com.guseeit.dto.GameRoundView;
import com.guseeit.dto.GuessRequest;
import com.guseeit.dto.GuessResultView;
import com.guseeit.service.GameService;
import com.guseeit.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final JobService jobService;

    public GameController(GameService gameService, JobService jobService) {
        this.gameService = gameService;
        this.jobService = jobService;
    }

    @GetMapping("/dynasties")
    public Map<String, List<String>> dynasties() {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        result.put("dynasties", jobService.supportedDynasties());
        return result;
    }

    @GetMapping("/session")
    public Map<String, Object> session(
            @RequestParam(required = false) String dynasty,
            @RequestParam(required = false) String era,
            @RequestParam(defaultValue = "5") int count
    ) {
        List<GameRoundView> rounds = gameService.createSession(dynasty, era, count);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("rounds", rounds);
        result.put("total", rounds.size());
        return result;
    }

    @PostMapping("/guess")
    public GuessResultView guess(@Valid @RequestBody GuessRequest request) {
        return gameService.submitGuess(request);
    }

    @GetMapping("/reverse-city")
    public Map<String, String> reverseCity(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        return gameService.lookupCity(latitude, longitude);
    }
}
