package com.example.exe2update.controller;

import com.example.exe2update.entity.Product;
import com.example.exe2update.service.ArticlesService;
import com.example.exe2update.service.CategoryService;
import com.example.exe2update.service.ProductService;
import com.example.exe2update.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ArticlesService articlesService;

    // @GetMapping("/home")
    // public String home(Authentication authentication, Model model) {
    // String username = authentication.getName();

    // List<Product> countRatesAndReviews = productService.getAllProducts();
    // // Map chứa productId -> số lượng review
    // Map<Integer, Long> reviewCounts = new HashMap<>();
    // // Map chứa productId -> rating trung bình
    // Map<Integer, Double> avgRatings = new HashMap<>();

    // for (Product product : countRatesAndReviews) {
    // Integer productId = product.getProductId();
    // reviewCounts.put(productId,
    // reviewService.getReviewCountByProductId(productId));
    // avgRatings.put(productId,
    // reviewService.getAverageRatingByProductId(productId));
    // }

    // model.addAttribute("username", username);
    // model.addAttribute("categories", categoryService.getAllCategories());
    // model.addAttribute("products", productService.getTop10Products());
    // model.addAttribute("newProducts", productService.getNew10Products());
    // model.addAttribute("top3newproducts", productService.getNewProducts());
    // model.addAttribute("reviewCounts", reviewCounts);
    // model.addAttribute("avgRatings", avgRatings);
    // model.addAttribute("articles",
    // articlesService.getAllArticlesWithAuthorName());
    // model.addAttribute("topSellingProducts",
    // productService.getTopSellingProducts(3));
    // model.addAttribute("trendingProducts",
    // productService.getTrendingProductsByReviewCount(3));

    // return "home";
    // }
    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        String username = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }

        // Các xử lý khác bình thường
        List<Product> countRatesAndReviews = productService.getAllProducts();

        Map<Integer, Long> reviewCounts = new HashMap<>();
        Map<Integer, Double> avgRatings = new HashMap<>();

        for (Product product : countRatesAndReviews) {
            Integer productId = product.getProductId();
            reviewCounts.put(productId, reviewService.getReviewCountByProductId(productId));
            avgRatings.put(productId, reviewService.getAverageRatingByProductId(productId));
        }

        model.addAttribute("username", username);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("products", productService.getTop10Products());
        model.addAttribute("newProducts", productService.getNew10Products());
        model.addAttribute("top3newproducts", productService.getNewProducts());
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("avgRatings", avgRatings);
        model.addAttribute("articles", articlesService.getAllArticlesWithAuthorName());
        model.addAttribute("topSellingProducts", productService.getTopSellingProducts(3));
        model.addAttribute("trendingProducts", productService.getTrendingProductsByReviewCount(3));

        return "home";
    }

}
