package com.guseeit.controller;

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
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.trim().isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "code 不能为空");
            return ResponseEntity.badRequest().body(err);
        }

        String token = userService.login(code);
        if (token == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "登录失败，请重试");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/phone")
    public ResponseEntity<Map<String, Object>> bindPhone(
            @RequestBody Map<String, String> body,
            @RequestParam(required = false) String token) {

        Long userId = userService.resolveUserId(token);
        if (userId == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        String encryptedData = body.get("encryptedData");
        String iv = body.get("iv");
        String phone = userService.bindPhone(userId, encryptedData, iv);

        Map<String, Object> result = new LinkedHashMap<>();
        if (phone != null) {
            result.put("phone", phone);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "手机号解密失败");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
}
