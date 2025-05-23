package com.example.exe2update.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.exe2update.entity.Role;
import com.example.exe2update.entity.User;
import com.example.exe2update.entity.VerificationToken;
import com.example.exe2update.repository.RoleRepository;
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

    @Autowired
    private RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("registerError", "Mật khẩu không khớp");
            redirectAttributes.addFlashAttribute("registerFullName", fullName);
            redirectAttributes.addFlashAttribute("registerEmail", email);
            redirectAttributes.addFlashAttribute("registerPhone", phone);
            return "redirect:/login";
        }

        if (userRepository.findByEmailNormalized(email).isPresent()) {
            redirectAttributes.addFlashAttribute("registerError", "Email đã được đăng ký.");
            return "redirect:/login";
        }

        if (tokenRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("registerError", "Email đang chờ xác thực. Kiểm tra email.");
            return "redirect:/login";
        }

        if (!phone.matches("\\d{9,11}")) {
            redirectAttributes.addFlashAttribute("registerError", "Số điện thoại không hợp lệ.");
            return "redirect:/login";
        }

        String token = UUID.randomUUID().toString();
        String passwordHash = passwordEncoder.encode(password);

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
                "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận.");
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
            tokenRepository.delete(verificationToken);
            return "redirect:/login";
        }

        if (userRepository.findByEmailNormalized(verificationToken.getEmail()).isPresent()) {
            redirectAttributes.addFlashAttribute("registerError", "Tài khoản đã xác thực.");
            tokenRepository.delete(verificationToken);
            return "redirect:/login";
        }

        User user = new User();
        user.setFullName(verificationToken.getFullName());
        user.setEmail(verificationToken.getEmail());
        user.setPhone(verificationToken.getPhone());
        user.setPasswordHash(verificationToken.getPasswordHash());
        user.setStatus(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUsername(verificationToken.getEmail().split("@")[0]);

        // Lấy role từ DB
        Optional<Role> userRoleOpt = roleRepository.findById(2);
        if (userRoleOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("registerError", "Không tìm thấy role USER.");
            return "redirect:/login";
        }

        user.setRole(userRoleOpt.get());

        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        redirectAttributes.addFlashAttribute("message", "Xác thực tài khoản thành công!");
        return "redirect:/login";
    }
}
