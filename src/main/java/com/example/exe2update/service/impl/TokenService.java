package com.example.exe2update.service.impl;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class TokenService {

    private Map<String, Token> tokenMap = new ConcurrentHashMap<>();

    public String createResetToken(String email) {
        String token = UUID.randomUUID().toString();
        Token tokenObj = new Token(token, email, System.currentTimeMillis() + 3600000); // Token có thời gian hết hạn 1 giờ
        tokenMap.put(token, tokenObj);
        return token;
    }

    public String getEmailByToken(String token) {
        Token tokenObj = tokenMap.get(token);
        if (tokenObj == null || tokenObj.isExpired()) {
            return null;  // Token không hợp lệ hoặc đã hết hạn
        }
        return tokenObj.getEmail();
    }

    public void removeToken(String token) {
        tokenMap.remove(token);
    }

    public static class Token {
        private String email;
        private long expiryTime;

        public Token(String token, String email, long expiryTime) {
            this.email = email;
            this.expiryTime = expiryTime;
        }

        public String getEmail() {
            return email;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
