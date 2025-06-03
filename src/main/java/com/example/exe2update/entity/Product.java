package com.example.exe2update.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @Column(length = 50) // PostgreSQL sẽ map sang VARCHAR(50)
    private String name;

    @Column(columnDefinition = "TEXT") // Thay NVARCHAR(MAX) bằng TEXT trong PostgreSQL
    private String description;

    private BigDecimal price;

    private Integer stock;

    @Column(length = 255)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "product")
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "product")
    private List<Cart> cartItems;

    @OneToMany(mappedBy = "product")
    private List<Review> reviews;

    private Double discount;

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%s, stock=%d, isActive=%b, discount=%f}",
                productId, name, price, stock, isActive, discount);
    }
}
