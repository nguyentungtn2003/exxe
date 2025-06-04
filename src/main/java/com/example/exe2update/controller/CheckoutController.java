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

    @PostMapping("/pay/vnpay")
    public RedirectView payByVNPay(HttpServletRequest request,
            Authentication auth,
            @RequestParam String fullName,
            @RequestParam String address,
            @RequestParam BigDecimal totalPrice) {
        try {
            String email = auth.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            // Tạo đơn hàng trước khi thanh toán
            Order order = new Order();
            order.setUser(user);
            order.setFullName(fullName);
            order.setAddress(address);
            order.setTotalAmount(totalPrice);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.Pending);
            orderService.save(order);
            // Tạo danh sách OrderDetail từ Cart
            List<Cart> cartItems = cartService.getCartByUser(user);
            List<OrderDetail> orderDetails = new ArrayList<>();

            for (Cart cart : cartItems) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProduct(cart.getProduct());
                detail.setQuantity(cart.getQuantity());

                // Lấy giá gốc
                double price = cart.getProduct().getPrice().doubleValue();

                // Lấy discount, giả sử kiểu Double, giá trị như 0.2 (20%)
                Double discount = cart.getProduct().getDiscount();
                if (discount == null)
                    discount = 0.0;

                // Tính giá sau giảm
                BigDecimal discountBD = BigDecimal.valueOf(1 - discount);
                BigDecimal finalPrice = BigDecimal.valueOf(price).multiply(discountBD);

                // Lưu giá thời điểm mua đã áp dụng giảm giá
                detail.setPrice(finalPrice);

                orderDetails.add(detail);
            }

            // Lưu chi tiết đơn hàng
            orderDetailRepository.saveAll(orderDetails); // hoặc orderDetailService.saveAll()

            // Lấy IP của client
            String ipAddr = getClientIp(request);

            // Tạo URL thanh toán VNPAY
            String paymentUrl = vnPayService.createPaymentUrl(
                    order.getOrderId().toString(),
                    totalPrice.longValue(),
                    ipAddr,
                    "Thanh Toan Don Hang " + order.getOrderId());

            // Redirect user đến VNPAY
            return new RedirectView(paymentUrl);

        } catch (Exception e) {
            log.error("Lỗi khi tạo URL thanh toán VNPay", e);
            return new RedirectView("/checkout?error=" + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @GetMapping("/vnpay-return")
    public String handleVnpayReturn(HttpServletRequest request, Model model, Authentication auth) {
        Map<String, String> fields = new HashMap<>();

        // Lấy tất cả tham số bắt đầu bằng "vnp_"
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];
            if (key.startsWith("vnp_")) {
                fields.put(key, value);
            }
        }

        // Lấy secure hash thực tế nhận được từ VNPAY
        String receivedSecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType"); // loại bỏ để không đưa vào hash

        // Sắp xếp tham số theo alphabet và tạo chuỗi hashData không encode
        String hashData = vnPayService.buildHashData(fields);

        // Tính lại hash với secret key
        String calculatedHash = vnPayService.hmacSHA512(vnPayService.getVnpHashSecret(), hashData);

        String orderIdStr = fields.get("vnp_TxnRef");
        String responseCode = fields.get("vnp_ResponseCode");
        String transactionStatus = fields.get("vnp_TransactionStatus");

        if (calculatedHash.equalsIgnoreCase(receivedSecureHash)) {
            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                // Thanh toán thành công => cập nhật trạng thái đơn hàng
                Integer orderId = Integer.valueOf(orderIdStr);
                orderService.updateOrderStatus(orderId, OrderStatus.Completed);
                cartService.removeFromCart(orderId);
                model.addAttribute("message", "Thanh toán thành công!");
                return "payment-result";
            } else {
                model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
                return "payment-result";
            }
        } else {
            model.addAttribute("message", "Dữ liệu không hợp lệ (sai chữ ký)");
            return "payment-result";
        }
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
            // String returnUrl = "https://exxe.onrender.com/payos-return?orderId=" +
            // order.getOrderId();
            String returnUrl = "https://exxe.onrender.com/home";
            String cancelUrl = "https://exxe.onrender.com/checkout?error=cancelled";

            String paymentUrl = payOSService.createPaymentUrl(
                    order.getOrderId(),
                    totalPrice.longValue(),
                    returnUrl,
                    cancelUrl);

            return new RedirectView(returnUrl);

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
            @RequestParam(required = false) Boolean cancel,
            @RequestParam(required = false) Integer orderId) {

        log.info("✅ Đã vào handlePayOSReturn với status={}, orderCode={}, cancel={}", status, orderCode, cancel);

        if ("PAID".equalsIgnoreCase(status) && Boolean.FALSE.equals(cancel)) {
            try {
                // Cập nhật trạng thái đơn hàng
                orderService.updateOrderStatus(orderId.intValue(), OrderStatus.Completed);
                cartService.removeFromCart(orderId); // hoặc theo user
                return new RedirectView("/home?payment=success");
            } catch (Exception e) {
                log.error("❌ Lỗi cập nhật đơn hàng: {}", e.getMessage());
                return new RedirectView("/checkout?error=update_failed");
            }
        } else {
            return new RedirectView("/checkout?error=cancelled_or_failed");
        }
    }

}
