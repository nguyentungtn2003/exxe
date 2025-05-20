package com.example.exe2update.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    // void sendOtpEmail(String to, String otp) throws MessagingException;
    void sendResetLink(String to, String resetLink) throws MessagingException;

    void sendVerificationEmail(String toEmail, String token);

}
