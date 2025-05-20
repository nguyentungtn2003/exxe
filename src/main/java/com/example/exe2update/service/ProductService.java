package com.example.exe2update.service;

import com.example.exe2update.dto.Response.PageResponse;
import com.example.exe2update.entity.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {
    List<Product> getNew10Products();

    List<Product> getTop10Products();

    List<Product> getNewProducts();

    List<Product> getAllProducts();

    Product getProductById(int id);

    List<Product> findByNameContainingIgnoreCase(String name);

    PageResponse<Product> getPagedProducts(int page, int size);

    PageResponse<Product> getFilteredProducts(String searchKeyword,
            List<Integer> categoryIds,
            List<String> discountRanges,
            int page,
            int size);

    Product findById(Integer productId);

    void saveProduct(Product product);

    List<Product> getTopSellingProducts(int limit);

    List<Product> getTrendingProductsByReviewCount(int limit);

}
