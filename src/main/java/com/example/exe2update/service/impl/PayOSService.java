package com.example.exe2update.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PayOSService {

    @Value("${payos.clientId}")
    private String clientId;

    @Value("${payos.apiKey}")
    private String apiKey;

    @Value("${payos.checksumKey}")
    private String checksumKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createPaymentUrl(Integer orderId, Long amount, String returnUrl, String cancelUrl) throws Exception {
        String endpoint = "https://api.payos.vn/v1/payment-requests";

        Map<String, Object> body = new HashMap<>();
        body.put("orderCode", orderId);
        body.put("amount", amount);
        body.put("description", "Thanh toán đơn hàng #" + orderId);
        body.put("returnUrl", returnUrl);
        body.put("cancelUrl", cancelUrl);

        // Checksum
        String rawData = clientId + "|" + orderId + "|" + amount + "|" + returnUrl + "|" + cancelUrl;
        String checksum = hmacSHA256(checksumKey, rawData);
        body.put("signature", checksum);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            return responseBody.get("checkoutUrl").toString();
        } else {
            throw new RuntimeException("Tạo thanh toán thất bại: " + response.getBody());
        }
    }

    public String hmacSHA256(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
