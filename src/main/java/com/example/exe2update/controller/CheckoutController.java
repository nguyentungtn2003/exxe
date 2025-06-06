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

    /**
     * BƯỚC 1: Chỉ tạo payment link, chưa lưu order
     */
    @PostMapping("/pay/payos")
    public RedirectView payByPayOS(
            Authentication auth,
            @RequestParam String fullName,
            @RequestParam String address) {
        try {
            String email = auth.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            List<Cart> cartItems = cartService.getCartByUser(user);
            if (cartItems.isEmpty()) {
                return new RedirectView("/checkout?error=Giỏ+hàng+trống");
            }

            // Tính tổng tiền
            long totalAmount = cartItems.stream()
                    .mapToLong(cart -> cart.getProduct().getPrice().longValue() * cart.getQuantity())
                    .sum();

            // Tạo orderCode tạm thời: bạn có thể dùng thời gian hoặc một mã random
            // vì lúc này chưa lưu DB => tạm thời có thể lấy current timestamp (nhưng phải
            // map được lại lúc thanh toán thành công)
            // Cách chuẩn là tạo bảng tạm hoặc tạo order sau khi thanh toán thành công, mình
            // demo đơn giản dùng timestamp.
            int orderCode = (int) (System.currentTimeMillis() / 1000);

            // String returnUrl = "http://localhost:8080/checkout/payos-return?orderCode=" +
            // orderCode + "&fullName="
            // + URLEncoder.encode(fullName, StandardCharsets.UTF_8) + "&address="
            // + URLEncoder.encode(address, StandardCharsets.UTF_8) + "&email="
            // + URLEncoder.encode(email, StandardCharsets.UTF_8);
            // String cancelUrl = "http://localhost:8080/checkout?error=cancelled";
            String returnUrl = "https://exxe.onrender.com/checkout/payos-return?orderCode=" + orderCode
                    + "&fullName=" + URLEncoder.encode(fullName, StandardCharsets.UTF_8)
                    + "&address=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
                    + "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);

            String cancelUrl = "https://exxe.onrender.com/checkout?error=cancelled";

            String paymentUrl = payOSService.createPaymentUrl(
                    orderCode,
                    totalAmount,
                    returnUrl,
                    cancelUrl);

            // Redirect đến link thanh toán của PayOS (có QR code)
            return new RedirectView(paymentUrl);

        } catch (Exception e) {
            log.error("Lỗi tạo URL thanh toán PayOS", e);
            String errorEncoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new RedirectView("/checkout?error=" + errorEncoded);
        }
    }

    /**
     * BƯỚC 2: Xử lý callback khi PayOS trả về, lúc này lưu đơn hàng và xóa giỏ hàng
     */
    @GetMapping("/payos-return")
    public RedirectView handlePayOSReturn(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean cancel) {

        log.info("✅ Đã vào handlePayOSReturn với status={}, orderCode={}, cancel={}", status, orderCode, cancel);

        if ("PAID".equalsIgnoreCase(status) && Boolean.FALSE.equals(cancel)) {
            try {
                if (orderCode == null || fullName == null || address == null || email == null) {
                    throw new RuntimeException("Thiếu tham số bắt buộc");
                }

                // Tìm user theo email
                User user = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

                // Lấy giỏ hàng user
                List<Cart> cartItems = cartService.getCartByUser(user);
                if (cartItems.isEmpty()) {
                    log.warn("Giỏ hàng trống khi thanh toán thành công");
                    return new RedirectView("/checkout?error=Giỏ+hàng+trống");
                }

                // Tính tổng tiền
                BigDecimal totalAmount = BigDecimal.valueOf(cartItems.stream()
                        .mapToLong(cart -> cart.getProduct().getPrice().longValue() * cart.getQuantity())
                        .sum());

                // Tạo đơn hàng mới
                Order order = new Order();
                order.setUser(user);
                order.setFullName(fullName);
                order.setAddress(address);
                order.setTotalAmount(totalAmount);
                order.setOrderDate(LocalDateTime.now());
                order.setStatus(OrderStatus.Completed); // Thanh toán thành công

                orderService.save(order);

                // Tạo chi tiết đơn hàng
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

                // Xóa giỏ hàng
                cartService.clearCart(user);

                return new RedirectView("/home");

            } catch (Exception e) {
                log.error("❌ Lỗi cập nhật đơn hàng: {}", e.getMessage());
                String errorEncoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                return new RedirectView("/checkout?error=" + errorEncoded);
            }
        } else {
            return new RedirectView("/checkout?error=cancelled_or_failed");
        }
    }
}
