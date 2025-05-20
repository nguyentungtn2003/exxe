package com.example.exe2update;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
public class MailTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void sendMailTest() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("tungnthe176064@gmail.com");
        message.setSubject("Test Mail");
        message.setText("Đây là test gửi mail.");
        mailSender.send(message);
    }
}
