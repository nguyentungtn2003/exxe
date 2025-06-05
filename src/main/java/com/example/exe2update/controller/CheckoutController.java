package com.example.exe2update.controller;

import com.example.exe2update.entity.*;
import com.example.exe2update.service.CartService;
import com.example.exe2update.service.OrderService;
import com.example.exe2update.service.UserService;
import com.example.exe2update.service.impl.PayOSService;
import com.example.exe2update.service.impl.VNPayService;
import com.example.exe2update.repository.OrderDetailRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import java.net.URLEncoder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;
    private final VNPayService vnPayService;
    private final OrderDetailRepository orderDetailRepository;
    private final PayOSService payOSService;

    @GetMapping
    public String showCheckoutPage(Authentication auth, Model model,
            @RequestParam(value = "error", required = false) String error) {
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<Cart> cartItems = cartService.getCartByUser(user);
        BigDecimal totalPrice = BigDecimal.valueOf(cartService.calculateTotalByUser(user));

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("username", user.getEmail());

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "checkout";
    }

    @PostMapping("/pay/payos")
    public RedirectView payByPayOS(
            Authentication auth,
            @RequestParam String fullName,
            @RequestParam String address,
            @RequestParam BigDecimal totalPrice) {
        try {
            String email = auth.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Order order = new Order();
            order.setUser(user);
            order.setFullName(fullName);
            order.setAddress(address);
            order.setTotalAmount(totalPrice);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.Pending);
            orderService.save(order);

            List<Cart> cartItems = cartService.getCartByUser(user);
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (Cart cart : cartItems) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProduct(cart.getProduct());
                detail.setQuantity(cart.getQuantity());
                detail.setPrice(cart.getProduct().getPrice());
                orderDetails.add(detail);
            }
            orderDetailRepository.saveAll(orderDetails);

            // Trả về endpoint xử lý kết quả thanh toán
            String returnUrl = "http://localhost:8080/checkout/payos-return";
            String cancelUrl = "http://localhost:8080/checkout?error=cancelled";

            String paymentUrl = payOSService.createPaymentUrl(
                    order.getOrderId(),
                    totalPrice.longValue(),
                    returnUrl,
                    cancelUrl);

            return new RedirectView(paymentUrl);

        } catch (Exception e) {
            log.error("Lỗi tạo URL thanh toán PayOS", e);
            String errorEncoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new RedirectView("/checkout?error=" + errorEncoded);
        }
    }

    @GetMapping("/payos-return")
    public RedirectView handlePayOSReturn(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) Boolean cancel) {

        log.info("✅ Đã vào handlePayOSReturn với status={}, orderCode={}, cancel={}", status, orderCode, cancel);

        if ("PAID".equalsIgnoreCase(status) && Boolean.FALSE.equals(cancel)) {
            try {
                if (orderCode == null) {
                    throw new RuntimeException("orderCode is null");
                }

                Long orderCodeLong = Long.parseLong(orderCode);
                // Update trạng thái đơn hàng theo orderCode (tức là orderId)
                orderService.updateOrderStatus(orderCodeLong.intValue(), OrderStatus.Completed);

                // Nếu muốn xóa giỏ hàng theo user, cần lấy user qua order:
                Order order = orderService.findByOrderId(orderCodeLong.intValue());
                if (order != null && order.getUser() != null) {
                    cartService.clearCart(order.getUser());
                }

                return new RedirectView("/home");
            } catch (Exception e) {
                log.error("❌ Lỗi cập nhật đơn hàng: {}", e.getMessage());
                return new RedirectView("/checkout?error=update_failed");
            }
        } else {
            return new RedirectView("/checkout?error=cancelled_or_failed");
        }
    }

}
