package com.example.exe2update.repository;

import com.example.exe2update.entity.Cart;
import com.example.exe2update.entity.Product;
import com.example.exe2update.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByUser(User user);
    Cart findByUserAndProduct(User user, Product product);
     void deleteByUser(User user);

}