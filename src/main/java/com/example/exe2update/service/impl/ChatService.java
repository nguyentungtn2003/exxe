package com.example.exe2update.service.impl;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatService {

    private static final String OPENAI_API_URL = "https://api.novita.ai/v3/openai/chat/completions";
    private static final String API_KEY = "sk_5tMudfggC9dKcZYDruKnujQI0-WkOh-1oBOXPlZc1io";

    public String askChatGPT(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Chuẩn bị body request
        Map<String, Object> requestBody = new HashMap<>();
        // Thay model thành model Novita có hỗ trợ
        requestBody.put("model", "meta-llama/llama-3.1-8b-instruct");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        // 2. Chuẩn bị headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        // 3. Tạo request
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. Gửi POST request
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                        return (String) messageObj.get("content");
                    }
                }
            }

            return "Không nhận được phản hồi từ ChatGPT.";
        } catch (Exception ex) {
            return "Lỗi khi gọi ChatGPT: " + ex.getMessage();
        }
    }
}
