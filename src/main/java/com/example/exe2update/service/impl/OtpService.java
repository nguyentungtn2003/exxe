package com.example.exe2update.service.impl;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class OtpService {

    private final ConcurrentHashMap<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    public String generateOtp(String email) {
        String otp = String.format("%06d", (int)(Math.random() * 1000000)); // Tạo OTP ngẫu nhiên 6 chữ số
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(10))); // Lưu OTP vào bộ nhớ
        System.out.println("Generated OTP for " + email + ": " + otp); // In ra OTP để kiểm tra
        return otp;
    }

    // Kiểm tra tính hợp lệ của OTP
    public boolean validateOtp(String email, String otp) {
        OtpData data = otpStorage.get(email);

        // Kiểm tra xem có tồn tại OTP cho email không, OTP có đúng không và có còn hiệu lực không
        if (data != null && data.getOtp().equals(otp) && LocalDateTime.now().isBefore(data.getExpireTime())) {
            otpStorage.remove(email);  // Xóa OTP khi đã xác thực thành công
            return true;
        }
        return false;
    }

    private static class OtpData {
        private final String otp;
        private final LocalDateTime expireTime;

        public OtpData(String otp, LocalDateTime expireTime) {
            this.otp = otp;
            this.expireTime = expireTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }
    }
}
