package com.example.exe2update.service.impl;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPayService {

    @Value("${vnpay.vnp_TmnCode}")
    private String vnpTmnCode;

    @Getter
    @Value("${vnpay.vnp_HashSecret}")
    private String vnpHashSecret;

    @Value("${vnpay.vnp_PayUrl}")
    private String vnpPayUrl;

    @Value("${vnpay.vnp_ReturnUrl}")
    private String vnpReturnUrl;

    public String createPaymentUrl(String orderId, long amount, String ipAddr, String orderInfo) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", vnpTmnCode);
            params.put("vnp_Amount", String.valueOf(amount * 100)); // nhân 100 theo quy định của VNPay
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", orderId);
            params.put("vnp_OrderInfo", orderInfo);
            params.put("vnp_OrderType", "other");
            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", vnpReturnUrl);
            params.put("vnp_IpAddr", ipAddr);

            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            String createDate = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            params.put("vnp_CreateDate", createDate);

            // Sắp xếp tham số theo thứ tự alphabet
            List<String> sortedKeys = new ArrayList<>(params.keySet());
            Collections.sort(sortedKeys);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String key : sortedKeys) {
                String value = params.get(key);
                hashData.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
            }

            // Xoá dấu '&' cuối
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
                query.setLength(query.length() - 1);
            }

            // Tạo HMAC SHA512
            String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());
            query.append("&vnp_SecureHashType=HMACSHA512"); // Viết hoa đúng chuẩn
            query.append("&vnp_SecureHash=").append(secureHash);

            return vnpPayUrl + "?" + query.toString();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo URL thanh toán VNPay", e);
        }
    }

    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi mã hoá HMAC SHA512", ex);
        }
    }

    public String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                // Ở đây phải encode giống hệt lúc tạo URL (dùng URLEncoder với charset US_ASCII)
                sb.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }



}
