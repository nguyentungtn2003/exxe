package com.example.exe2update.controller;

import com.example.exe2update.entity.Order;
import com.example.exe2update.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // @GetMapping
    // public String dashboard(Model model) {
    // // Lấy dữ liệu tóm tắt dashboard (nếu có)
    // Map<String, Object> data = dashboardService.getDashboardSummary();
    // model.addAllAttributes(data);

    // // Thống kê doanh thu
    // Map<String, Long> revenueByMonth = dashboardService.getRevenueByMonth(1, 6,
    // 2024);
    // model.addAttribute("revenueByMonth", revenueByMonth);

    // return "dashboard"; // Giao diện Thymeleaf: dashboard.html
    // }
    @GetMapping
    public String dashboard(Model model, Authentication authentication) throws Exception {
        Map<String, Object> data = dashboardService.getDashboardSummary();
        model.addAllAttributes(data);
        String username = authentication.getName();
        Map<String, Long> revenueByMonth = dashboardService.getRevenueByMonth(1, 12, 2025);
        model.addAttribute("revenueByMonth", revenueByMonth);

        // Convert Map sang JSON String
        ObjectMapper mapper = new ObjectMapper();
        String revenueByMonthJson = mapper.writeValueAsString(revenueByMonth);
        model.addAttribute("revenueByMonthJson", revenueByMonthJson);

        // revenue by month test sql
        // SELECT
        // MONTH(order_date) AS Thang,
        // SUM(total_amount) AS DoanhThu
        // FROM
        // [fina2l].[dbo].[orders]
        // WHERE
        // MONTH(order_date) BETWEEN 1 AND 6
        // AND YEAR(order_date) = 2024 -- ⚠️ bạn có thể sửa theo năm bạn muốn
        // GROUP BY
        // MONTH(order_date)
        // ORDER BY
        // Thang;
        // Lấy top 3 sản phẩm bán chạy nhất (List<Object[]>)
        List<Object[]> top3Products = dashboardService.getTop3BestSellingProducts();
        model.addAttribute("top3Products", top3Products);

        List<Order> recentOrders = dashboardService.getRecentOrders(4);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("username", username);
        return "dashboard";
    }

}
