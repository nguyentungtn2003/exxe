package com.example.exe2update.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;  // Each cart belongs to a user.

    @ManyToOne
    @JoinColumn(name = "productID", nullable = false)
    private Product product;  // Each cart item corresponds to a product.

    private Integer quantity;  // Number of the product in the cart.

    @Column(nullable = false)
    private String productName;  // Product name (stored separately in the cart).

    @Column(nullable = false)
    private BigDecimal productPrice;  // Product price (stored separately in the cart).

    private String productImage;  // Product image URL (stored separately in the cart).


    private Double discount;  // Discount on the product (if any).

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // Creation timestamp for the cart item.

    public Double getTotalPrice() {
        if (productPrice != null && quantity != null) {
            return productPrice.multiply(BigDecimal.valueOf(quantity)).doubleValue();
        }
        return 0.0;
    }

    // Ensuring createdAt gets set before inserting a new cart item
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();  // Set the current timestamp for the cart creation.
        }
    }

    // Method to update Cart info from Product (in case product details change)
    public void updateFromProduct(Product product) {
        this.productName = product.getName();
        this.productPrice = product.getPrice();
        this.productImage = product.getImageUrl();
    }
}
