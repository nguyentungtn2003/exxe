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

    @Column(columnDefinition = "NVARCHAR(50)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private BigDecimal price;

    private Integer stock;

    @Column(length = 255)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "categoryID")
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

    private Double discount; // Tỷ lệ giảm, ví dụ: 0.2 nghĩa là giảm 20%

    // Tự định nghĩa lại phương thức toString() để tránh vòng lặp vô hạn
    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%s, stock=%d, isActive=%b, discount=%f}",
                productId, name, price, stock, isActive, discount);
    }

    // Getters and Setters
}
