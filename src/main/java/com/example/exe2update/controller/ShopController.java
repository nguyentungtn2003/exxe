// ShopController.java
package com.example.exe2update.controller;

import com.example.exe2update.dto.Response.PageResponse;
import com.example.exe2update.entity.Product;
import com.example.exe2update.service.CategoryService;
import com.example.exe2update.service.ProductService;
import com.example.exe2update.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/shop")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String shop(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "8") int size,
                       Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        PageResponse<Product> response = productService.getPagedProducts(page, size);
        List<Product> products = response.getContent();

        Map<Integer, Long> reviewCounts = new HashMap<>();
        Map<Integer, Double> avgRatings = new HashMap<>();
        populateReviewData(products, reviewCounts, avgRatings);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("avgRatings", avgRatings);
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("username", username);

        return "shop";
    }

    @GetMapping("/filter")
    public String filterProducts(@RequestParam(defaultValue = "") String search,
                                 @RequestParam(required = false) List<Integer> category,
                                 @RequestParam(required = false) List<String> discount,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "8") int size,
                                 Model model) {

        category = category == null ? List.of() : category;
        discount = discount == null ? List.of() : discount;

        page = Math.max(page, 0);
        size = size <= 0 ? 8 : size;

        PageResponse<Product> response = productService.getFilteredProducts(search, category, discount, page, size);
        List<Product> products = response.getContent();

        Map<Integer, Long> reviewCounts = new HashMap<>();
        Map<Integer, Double> avgRatings = new HashMap<>();
        populateReviewData(products, reviewCounts, avgRatings);
        System.out.println(products);
        model.addAttribute("products", products);
        model.addAttribute("avgRatings", avgRatings);
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("discount", discount);

        return "shop :: productList";
    }

    private void populateReviewData(List<Product> products, Map<Integer, Long> reviewCounts, Map<Integer, Double> avgRatings) {
        for (Product product : products) {
            Integer productId = product.getProductId();
            reviewCounts.put(productId, reviewService.getReviewCountByProductId(productId));
            avgRatings.put(productId, reviewService.getAverageRatingByProductId(productId));
        }
    }
}
