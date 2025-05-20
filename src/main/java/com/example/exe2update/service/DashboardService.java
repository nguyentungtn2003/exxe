package com.example.exe2update.service;

import java.util.List;
import java.util.Map;

import com.example.exe2update.entity.Order;

public interface DashboardService {
    Map<String, Object> getDashboardSummary();

    Map<String, Long> getRevenueByMonth(int fromMonth, int toMonth, int year);

    List<Object[]> getTop3BestSellingProducts();

    List<Order> getRecentOrders(int limit);
}
