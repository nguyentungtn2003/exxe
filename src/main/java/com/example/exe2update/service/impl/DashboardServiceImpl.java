package com.example.exe2update.service.impl;

import com.example.exe2update.entity.Order;
import com.example.exe2update.repository.*;
import com.example.exe2update.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> data = new HashMap<>();

        Double totalRevenueDouble = orderRepository.calculateTotalRevenue();
        Long totalRevenueLong = totalRevenueDouble != null ? totalRevenueDouble.longValue() : 0L;

        Long totalOrders = orderRepository.count();
        Long totalProducts = productRepository.count();
        Long totalCustomers = userRepository.countByRole_RoleName("USER");

        // Format totalRevenue trước khi put vào map
        String totalRevenueFormatted = formatCurrency(totalRevenueLong);

        data.put("totalRevenue", totalRevenueFormatted);
        data.put("totalOrders", totalOrders);
        data.put("totalProducts", totalProducts);
        data.put("totalCustomers", totalCustomers);
        data.put("allCategories", categoryRepository.findAll());

        return data;
    }

    public String formatCurrency(Long value) {
        if (value == null)
            return "0 VNĐ";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(value) + " VNĐ";
    }

    @Override
    public Map<String, Long> getRevenueByMonth(int fromMonth, int toMonth, int year) {
        List<Object[]> rows = orderRepository.getRevenueByMonth(fromMonth, toMonth, year);
        Map<String, Long> result = new LinkedHashMap<>();

        // Khởi tạo map với tất cả các tháng trước để tránh thiếu tháng không có đơn
        for (int month = fromMonth; month <= toMonth; month++) {
            result.put("Tháng " + month, 0L);
        }

        for (Object[] row : rows) {
            Integer month = ((Number) row[0]).intValue();
            Long revenue = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            result.put("Tháng " + month, revenue);
        }

        return result;
    }

    @Override
    public List<Object[]> getTop3BestSellingProducts() {
        return productRepository.findTopProductsByQuantitySold(PageRequest.of(0, 3));
    }

    @Override
    public List<Order> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "orderDate"));
        return orderRepository.findAll(pageable).getContent();
    }

}
