package com.example.exe2update.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.exe2update.entity.Order;
import com.example.exe2update.entity.User;
import com.example.exe2update.service.OrderService;
import com.example.exe2update.service.UserService;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderTrackingController {

    private final UserService userService; // Để lấy user từ email
    private final OrderService orderService;

    @GetMapping("/order-tracking")
    public String orderTrackingPage(Authentication authentication, Model model) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> completedOrders = orderService.getCompletedOrdersByUserId(user.getUserId());

        model.addAttribute("username", user.getEmail());
        model.addAttribute("orders", completedOrders);

        return "order-tracking";
    }
}
