package com.example.exe2update.repository;

import com.example.exe2update.entity.Product;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, ProductRepositoryCustom {
        List<Product> findTop10ByOrderByCreatedAtDesc();

        List<Product> findTop10ByOrderByCreatedAtAsc();

        List<Product> findTop3ByOrderByCreatedAtDesc();

        List<Product> findByNameContainingIgnoreCase(String name);

        @Query("SELECT p, SUM(od.quantity) AS totalSold " +
                        "FROM OrderDetail od JOIN od.product p " +
                        "GROUP BY p " +
                        "ORDER BY totalSold DESC")
        List<Object[]> findTopProductsByQuantitySold(PageRequest pageable);

        @Query("SELECT p FROM Product p JOIN OrderDetail od ON p.productId = od.product.productId " +
                        "GROUP BY p " +
                        "ORDER BY SUM(od.quantity) DESC")
        List<Product> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);

        @Query("SELECT p FROM Product p LEFT JOIN Review r ON p.productId = r.product.productId " +
                        "GROUP BY p " +
                        "ORDER BY COUNT(r.reviewId) DESC")
        List<Product> findTrendingProductsByReviewCount(org.springframework.data.domain.Pageable pageable);

}
