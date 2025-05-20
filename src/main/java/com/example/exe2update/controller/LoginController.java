package com.example.exe2update.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model, HttpServletRequest request) {

        if (error != null) {
            model.addAttribute("message", request.getSession().getAttribute("message"));
            model.addAttribute("lastEmail", request.getSession().getAttribute("lastEmail"));
            model.addAttribute("showForgotPassword", request.getSession().getAttribute("showForgotPassword"));
        }

        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công.");
        }

        return "login";
    }

    // Xử lý đăng nhập
    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
            Model model, HttpServletRequest request) {
        // Nếu đăng nhập không thành công, hệ thống sẽ tự động chuyển đến
        // CustomAuthenticationFailureHandler
        return "redirect:/login?error"; // Nếu có lỗi, sẽ tự động quay lại trang login
    }

}
