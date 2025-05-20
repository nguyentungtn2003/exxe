package com.example.exe2update;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

public class VnpaySignatureTest {

    // Hàm tạo chữ ký HMAC SHA512
    private String hmacSHA512(String key, String data) {
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

    // Hàm build chuỗi hash data theo tham số VNPAY (sắp xếp key và nối lại)
    private String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(fieldName).append('=').append(fieldValue);
            }
        }
        return sb.toString();
    }

    @Test
    public void testSignatureConsistency() {
        // Secret key (chuỗi bí mật) lấy từ VNPAY
        String secretKey = "D82X0VM2XFVX80V222RV5V4GEYNYIH0J";

        // Mô phỏng tham số nhận từ VNPAY (không có vnp_SecureHash)
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "8000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_BankTranNo", "VNP14958942");
        params.put("vnp_CardType", "ATM");
        params.put("vnp_OrderInfo", "Thanh Toan Don Hang61");
        params.put("vnp_PayDate", "20250515154802");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TmnCode", "DS8AUUNZ");
        params.put("vnp_TransactionNo", "14958942");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TxnRef", "61");

        // Tạo chuỗi hashData
        String hashData = buildHashData(params);
        System.out.println("Chuỗi hashData: " + hashData);

        // Tạo chữ ký 2 lần để test tính nhất quán
        String signature1 = hmacSHA512(secretKey, hashData);
        String signature2 = hmacSHA512(secretKey, hashData);

        System.out.println("Chữ ký 1: " + signature1);
        System.out.println("Chữ ký 2: " + signature2);

        // So sánh 2 chữ ký phải bằng nhau
        assertEquals(signature1, signature2, "Chữ ký tạo ra không nhất quán");
    }
}
