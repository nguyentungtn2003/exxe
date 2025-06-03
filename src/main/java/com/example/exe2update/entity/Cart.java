package com.example.exe2update.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart") // PostgreSQL mặc định để tên bảng chữ thường
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String productName;

    @Column(nullable = false)
    private BigDecimal productPrice;

    @Column(columnDefinition = "TEXT")
    private String productImage;

    private Double discount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Double getTotalPrice() {
        if (productPrice != null && quantity != null) {
            return productPrice.multiply(BigDecimal.valueOf(quantity)).doubleValue();
        }
        return 0.0;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void updateFromProduct(Product product) {
        this.productName = product.getName();
        this.productPrice = product.getPrice();
        this.productImage = product.getImageUrl();
    }
}
