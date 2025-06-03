package com.example.exe2update.service.impl;

import com.example.exe2update.service.EmailService;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // @Override
    // public void sendOtpEmail(String to, String otp) throws MessagingException {
    // System.out.println("Bắt đầu gửi OTP tới: " + to);
    //
    // SimpleMailMessage message = new SimpleMailMessage();
    // message.setTo(to);
    // message.setSubject("Mã OTP của bạn");
    // message.setText("Mã OTP của bạn là: " + otp);
    //
    // mailSender.send(message);
    //
    // System.out.println("Gửi mail thành công đến: " + to);
    // }
    @Override
    public void sendResetLink(String to, String resetLink) throws MessagingException {
        System.out.println("Đang gửi liên kết đặt lại mật khẩu tới: " + to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Đặt lại mật khẩu của bạn");
        message.setText("Nhấn vào liên kết sau để đặt lại mật khẩu: " + resetLink +
                "\n\nLưu ý: Liên kết này chỉ sử dụng được một lần.");

        mailSender.send(message);

        System.out.println("Đã gửi liên kết đặt lại mật khẩu đến: " + to);
    }

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String link = "https://exxe.onrender.com/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác nhận tài khoản của bạn");
        message.setText("Nhấp vào link để xác nhận tài khoản: " + link);
        mailSender.send(message);
    }

}
