package com.example.exe2update.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

@Controller
public class ContactController {

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        // Lấy thông tin authentication hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Lấy username (ở đây là email, ví dụ: Gmail)
        String email = authentication.getName();

        // Gửi username sang giao diện Thymeleaf
        model.addAttribute("username", email);

        return "contact";
    }

}
