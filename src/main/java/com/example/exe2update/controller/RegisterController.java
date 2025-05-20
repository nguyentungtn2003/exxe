package com.example.exe2update.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.exe2update.entity.Role;
import com.example.exe2update.entity.User;
import com.example.exe2update.entity.VerificationToken;
import com.example.exe2update.repository.UserRepository;
import com.example.exe2update.repository.VerificationTokenRepository;
import com.example.exe2update.service.EmailService;

@Controller
public class RegisterController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra password khớp
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("registerError", "Mật khẩu không khớp");
            redirectAttributes.addFlashAttribute("registerFullName", fullName);
            redirectAttributes.addFlashAttribute("registerEmail", email);
            redirectAttributes.addFlashAttribute("registerPhone", phone);
            return "redirect:/login";
        }

        // Kiểm tra email đã tồn tại trong User (đã đăng ký xác thực)
        if (userRepository.findByEmailNormalized(email).isPresent()) {
            redirectAttributes.addFlashAttribute("registerError", "Email đã được đăng ký.");
            redirectAttributes.addFlashAttribute("registerFullName", fullName);
            redirectAttributes.addFlashAttribute("registerEmail", email);
            redirectAttributes.addFlashAttribute("registerPhone", phone);
            return "redirect:/login";
        }

        // Kiểm tra email đã tồn tại trong VerificationToken (đang chờ xác thực)
        if (tokenRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("registerError",
                    "Email đã được đăng ký và đang chờ xác thực. Vui lòng kiểm tra email.");
            redirectAttributes.addFlashAttribute("registerFullName", fullName);
            redirectAttributes.addFlashAttribute("registerEmail", email);
            redirectAttributes.addFlashAttribute("registerPhone", phone);
            return "redirect:/login";
        }

        // Kiểm tra số điện thoại cơ bản (vd: chỉ chứa số, độ dài 9-11)
        if (!phone.matches("\\d{9,11}")) {
            redirectAttributes.addFlashAttribute("registerError", "Số điện thoại không hợp lệ.");
            redirectAttributes.addFlashAttribute("registerFullName", fullName);
            redirectAttributes.addFlashAttribute("registerEmail", email);
            redirectAttributes.addFlashAttribute("registerPhone", phone);
            return "redirect:/login";
        }

        // Tạo token và hash password
        String token = UUID.randomUUID().toString();
        String passwordHash = new BCryptPasswordEncoder().encode(password);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setFullName(fullName);
        verificationToken.setEmail(email);
        verificationToken.setPhone(phone);
        verificationToken.setPasswordHash(passwordHash);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(email, token);

        redirectAttributes.addFlashAttribute("registerSuccess",
                "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận tài khoản.");

        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            redirectAttributes.addFlashAttribute("registerError", "Mã xác thực không hợp lệ.");
            return "redirect:/login";
        }

        VerificationToken verificationToken = optionalToken.get();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("registerError", "Mã xác thực đã hết hạn.");
            return "redirect:/login";
        }

        // Kiểm tra xem user đã tồn tại chưa
        Optional<User> existingUser = userRepository.findByEmailNormalized(verificationToken.getEmail());
        if (existingUser.isPresent()) {
            redirectAttributes.addFlashAttribute("registerError", "Tài khoản đã được xác thực trước đó.");
            tokenRepository.delete(verificationToken); // Xóa token để không dùng lại
            return "redirect:/login";
        }

        User user = new User();
        user.setFullName(verificationToken.getFullName());
        user.setEmail(verificationToken.getEmail());
        user.setPhone(verificationToken.getPhone());
        user.setPasswordHash(verificationToken.getPasswordHash());
        user.setStatus(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUsername("user"); // hoặc tạo username phù hợp

        Role role = new Role();
        role.setRoleId(2); // set role id = 2 (ví dụ role USER)
        user.setRole(role);

        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        redirectAttributes.addFlashAttribute("message", "Xác thực tài khoản thành công!");
        return "redirect:/login";
    }

}
