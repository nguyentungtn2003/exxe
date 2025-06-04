package com.example.exe2update.service;

import com.example.exe2update.entity.Cart;
import com.example.exe2update.entity.User;

import java.util.List;

public interface CartService {
    List<Cart> getCartByUser(User user);

    void addToCart(User user, Integer productId, Integer quantity);

    boolean updateQuantity(Integer cartId, Integer quantity);

    void removeFromCart(Integer cartId);

    double getProductPrice(Integer cartId);

    double calculateTotalByUser(User user);

    void clearCart(User user);

}
