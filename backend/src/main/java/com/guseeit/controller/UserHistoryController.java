package com.guseeit.controller;

import com.guseeit.dto.UserHistoryView;
import com.guseeit.service.UserHistoryService;
import com.guseeit.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserHistoryController {

    private final UserHistoryService historyService;
    private final UserService userService;

    public UserHistoryController(UserHistoryService historyService, UserService userService) {
        this.historyService = historyService;
        this.userService = userService;
    }

    @PostMapping("/history")
    public ResponseEntity<Map<String, Object>> save(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) String token) {

        Long userId = userService.resolveUserId(token);
        if (userId == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        // This is handled in GameService instead
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String token,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        Long userId = userService.resolveUserId(token);
        if (userId == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        int total = historyService.count(userId);
        List<UserHistoryView> rows = historyService.list(userId, offset, limit);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("total", total);
        result.put("hasMore", offset + rows.size() < total);
        return ResponseEntity.ok(result);
    }
}
