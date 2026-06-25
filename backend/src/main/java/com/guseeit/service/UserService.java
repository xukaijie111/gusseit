package com.guseeit.service;

import com.guseeit.client.DouyinClient;
import com.guseeit.domain.User;
import com.guseeit.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DouyinClient douyinClient;

    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository, DouyinClient douyinClient) {
        this.userRepository = userRepository;
        this.douyinClient = douyinClient;
    }

    public Long resolveUserId(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        return tokenStore.get(token);
    }

    public String login(String code) {
        DouyinClient.SessionInfo session = douyinClient.code2Session(code);
        if (session == null || session.getOpenid().isEmpty()) {
            return null;
        }

        Optional<User> existing = userRepository.findByOpenid(session.getOpenid());
        User user;
        if (existing.isPresent()) {
            user = existing.get();
            user.setSessionKey(session.getSessionKey());
        } else {
            user = new User();
            user.setOpenid(session.getOpenid());
            user.setSessionKey(session.getSessionKey());
        }
        userRepository.save(user);

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());
        return token;
    }

    public String bindPhone(Long userId, String encryptedData, String iv) {
        Optional<User> opt = userRepository.findById(userId);
        if (!opt.isPresent()) return null;
        User user = opt.get();
        if (user.getSessionKey() == null) return null;

        String phone = douyinClient.decryptPhone(encryptedData, iv, user.getSessionKey());
        if (phone == null || phone.isEmpty()) return null;

        user.setPhone(phone);
        userRepository.save(user);
        return phone;
    }
}
