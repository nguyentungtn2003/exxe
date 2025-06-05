package com.example.exe2update.service;

import java.util.List;

import com.example.exe2update.entity.*;

public interface OrderService {
    boolean createOrder(User user, List<Cart> cartItems);

    Order save(Order order);

    void saveOrderDetail(OrderDetail orderDetail);

    Order findByOrderId(Integer orderId);

    void updateOrderStatus(Integer orderId, OrderStatus status);

    int countCompletedOrdersByUserId(int userId);

    List<Order> getCompletedOrdersByUserId(Integer userId);
}
