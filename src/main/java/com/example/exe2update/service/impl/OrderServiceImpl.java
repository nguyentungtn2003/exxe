package com.example.exe2update.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.exe2update.entity.Cart;
import com.example.exe2update.entity.Order;
import com.example.exe2update.entity.OrderDetail;
import com.example.exe2update.entity.OrderStatus;
import com.example.exe2update.entity.User;
import com.example.exe2update.repository.CartRepository;
import com.example.exe2update.repository.OrderDetailRepository;
import com.example.exe2update.repository.OrderRepository;
import com.example.exe2update.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private CartRepository cartRepository;
  @Autowired
  private OrderDetailRepository orderDetailRepository;

  @Override
  public boolean createOrder(User user, List<Cart> cartItems) {
    // Tạo đơn hàng mới
    Order order = new Order();
    order.setUser(user);
    order.setOrderDate(LocalDateTime.now());
    order.setStatus(OrderStatus.Pending); // Đơn hàng mới sẽ có trạng thái "Đang chờ xử lý"

    // Tính tổng giá trị của đơn hàng
    BigDecimal totalAmount = cartItems.stream()
        .map(cart -> cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    order.setTotalAmount(totalAmount);

    // Lưu đơn hàng vào cơ sở dữ liệu
    Order savedOrder = orderRepository.save(order);

    // Tạo OrderDetails từ Cart
    for (Cart cartItem : cartItems) {
      OrderDetail orderDetail = new OrderDetail();
      orderDetail.setOrder(savedOrder);
      orderDetail.setProduct(cartItem.getProduct());
      orderDetail.setQuantity(cartItem.getQuantity());
      orderDetail.setPrice(cartItem.getProductPrice());

      orderDetailRepository.save(orderDetail); // Lưu chi tiết đơn hàng
    }

    // Xóa các sản phẩm trong giỏ hàng sau khi tạo đơn
    cartRepository.deleteAll(cartItems);

    return savedOrder != null;
  }

  @Override
  public Order save(Order order) {
    return orderRepository.save(order);
  }

  @Override
  public void saveOrderDetail(OrderDetail orderDetail) {
    orderDetailRepository.save(orderDetail);
  }

  @Override
  public Order findByOrderId(Integer orderId) {
    return orderRepository.findById(orderId).orElse(null);
  }

  @Override
  public void updateOrderStatus(Integer orderId, OrderStatus status) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
    order.setStatus(status);
    orderRepository.save(order);
  }

  @Override
  public int countCompletedOrdersByUserId(int userId) {
    return orderRepository.countByUser_UserIdAndStatus(userId, OrderStatus.Completed);
  }

  @Override
  public List<Order> getCompletedOrdersByUserId(Integer userId) {
    return orderRepository.findByUser_UserIdAndStatus(userId, OrderStatus.Completed);
  }
}
