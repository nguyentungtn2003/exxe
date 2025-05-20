package com.example.exe2update.service.impl;

import com.example.exe2update.dto.Response.PageResponse;
import com.example.exe2update.entity.Product;
import com.example.exe2update.repository.ProductRepository;
import com.example.exe2update.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getNew10Products() {
        return productRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Product> getTop10Products() {
        return productRepository.findTop10ByOrderByCreatedAtAsc();
    }

    @Override
    public List<Product> getNewProducts() {
        return productRepository.findTop3ByOrderByCreatedAtDesc();
    }

    @Override
    public PageResponse<Product> getPagedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        PageResponse<Product> response = new PageResponse<>();
        response.setContent(productPage.getContent());
        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());

        return response;
    }

    @Override
    public PageResponse<Product> getFilteredProducts(String searchKeyword,
            List<Integer> categoryIds,
            List<String> discountRanges,
            int page,
            int size) {
        // Gọi vào custom repo mà bạn đã triển khai findFiltered()
        Page<Product> p = productRepository.findFiltered(page, size, categoryIds, discountRanges, searchKeyword);
        return new PageResponse<>(p);
    }

    @Override
    public Product findById(Integer productId) {
        return productRepository.findById(productId).orElse(null); // Trả về null nếu không tìm thấy sản phẩm
    }

    @Override
    public void saveProduct(Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setIsActive(true);
        productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<Product> findByNameContainingIgnoreCase(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Product> getTopSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTopSellingProducts(pageable);
    }

    @Override
    public List<Product> getTrendingProductsByReviewCount(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTrendingProductsByReviewCount(pageable);
    }

}
