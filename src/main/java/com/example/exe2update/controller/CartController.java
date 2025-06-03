package com.example.exe2update.controller;

import com.example.exe2update.entity.User;
import com.example.exe2update.service.CartService;
import com.example.exe2update.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    // Hiển thị giỏ hàng
    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        model.addAttribute("username", user.getEmail());
        model.addAttribute("cartItems", cartService.getCartByUser(user));
        return "cart";
    }

    // Thêm sản phẩm vào giỏ bằng path variable (mặc định quantity = 1)
    @GetMapping("/add/{productId}")
    public String addToCartByPath(@PathVariable("productId") Integer productId,
            Authentication authentication) {
        return addToCart(productId, 1, authentication);
    }

    // Thêm sản phẩm vào giỏ bằng query param
    @GetMapping("/add")
    public String addToCartByParam(@RequestParam("productId") Integer productId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
            Authentication authentication) {
        return addToCart(productId, quantity, authentication);
    }

    // Logic chung thêm vào giỏ
    private String addToCart(Integer productId, Integer quantity, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        cartService.addToCart(user, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCart(@RequestParam("cartId") Integer cartId,
            @RequestParam("quantity") Integer quantity) {
        Map<String, Object> response = new HashMap<>();

        if (quantity <= 0) {
            response.put("success", false);
            response.put("message", "Số lượng phải lớn hơn 0");
            return response;
        }

        boolean updated = cartService.updateQuantity(cartId, quantity);

        if (updated) {
            // Lấy giá đã được nhân số lượng rồi, nên không nhân thêm quantity nữa
            double totalPrice = cartService.getProductPrice(cartId);

            response.put("success", true);
            response.put("totalPrice", totalPrice);
        } else {
            response.put("success", false);
            response.put("message", "Cập nhật thất bại. Kiểm tra số lượng sản phẩm trong kho.");
        }

        return response;
    }

    @DeleteMapping("/remove")
    public String removeFromCart(@RequestParam("cartId") Integer cartId) {
        cartService.removeFromCart(cartId);
        return "redirect:/cart";
    }

    @GetMapping("/total")
    @ResponseBody
    public Map<String, Object> getCartTotal(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        double total = cartService.calculateTotalByUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("total", total);
        return response;
    }

}
