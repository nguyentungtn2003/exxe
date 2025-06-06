package com.example.exe2update.controller;

import com.example.exe2update.service.EmailService;
import com.example.exe2update.service.UserService;
import com.example.exe2update.service.impl.TokenService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) throws MessagingException {
        String token = tokenService.createResetToken(email);
        String resetLink = "https://exxe.onrender.com/change-password?token=" + token;

        emailService.sendResetLink(email, resetLink);
        model.addAttribute("message", "Một liên kết đổi mật khẩu đã được gửi đến email.");
        return "login";
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(@RequestParam String token, Model model) {
        String email = tokenService.getEmailByToken(token);
        if (email == null) {
            model.addAttribute("message", "Liên kết không hợp lệ hoặc đã hết hạn.");
            return "login";
        }

        model.addAttribute("token", token);
        model.addAttribute("email", email);
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String email,
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("message", "Mật khẩu không khớp.");
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "change-password";
        }

        String tokenEmail = tokenService.getEmailByToken(token);
        if (tokenEmail == null || !tokenEmail.equals(email)) {
            model.addAttribute("message", "Token không hợp lệ hoặc email không khớp.");
            return "login";
        }

        userService.updatePasswordByEmail(email, newPassword);
        tokenService.removeToken(token);

        model.addAttribute("message", "Mật khẩu đã được thay đổi thành công.");
        return "login";
    }
}
