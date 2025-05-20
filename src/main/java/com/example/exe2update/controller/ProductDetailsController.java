package com.example.exe2update.controller;

import com.example.exe2update.entity.Product;
import com.example.exe2update.entity.Review;
import com.example.exe2update.entity.User;
import com.example.exe2update.service.ProductService;
import com.example.exe2update.service.ReviewService;
import com.example.exe2update.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
//
//@Controller
//
//public class ProductDetailsController {
//
//    @Autowired
//    private ProductService productService;
//
//    @Autowired
//    private ReviewService reviewService;
//
//    @Autowired
//    private UserService userService; // Đảm bảo có userService để truy vấn User từ cơ sở dữ liệu
//
//    // Hiển thị chi tiết sản phẩm và các đánh giá
//    @GetMapping("/product-details/{id}")
//    public String showProductDetails(@PathVariable("id") int id, Model model, Authentication authentication) {
//        String username = authentication.getName();
//        Product product = productService.getProductById(id);
//        Long reviewCount = reviewService.getReviewCountByProductId(id);
//        Double avgRating = reviewService.getAverageRatingByProductId(id);
//
//        if (avgRating == null || avgRating == 0.0) {
//            avgRating = 5.0;
//        }
//
//        var reviews = reviewService.getReviewsByProductId(id);
//
//        // Truyền thông tin người dùng vào model
//        if (username != null) {
//            model.addAttribute("username", username);
//        } else {
//            model.addAttribute("username", "Guest");
//        }
//
//        model.addAttribute("product", product);
//        model.addAttribute("reviewCount", reviewCount);
//        model.addAttribute("avgRating", avgRating);
//        model.addAttribute("reviews", reviews);
//        model.addAttribute("review", new Review());
//        Review newReview = new Review();
//        newReview.setRating(0); // Gán mặc định để tránh lỗi null
//        model.addAttribute("review", newReview);
//        return "product-details";
//    }
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
//    @PostMapping("/product-details/{id}/add-review")
//    public String addReview(@PathVariable("id") int id,
//                            @RequestParam("rating") int rating,  // Nhận giá trị rating từ form
//                            @RequestParam("comment") String comment,  // Nhận comment
//                            Authentication authentication,
//                            Model model) {
//
//        if (authentication != null) {
//            String username = authentication.getName();
//            Optional<User> optionalUser = userService.findByUsername(username);  // Trả về Optional<User>
//
//            if (optionalUser.isPresent()) {
//                User user = optionalUser.get();
//                Review review = new Review();
//                review.setRating(rating);  // Gán rating cho review
//                review.setComment(comment);  // Gán comment cho review
//                review.setUser(user);
//                review.setCreatedAt(LocalDateTime.now());
//
//                Product product = productService.getProductById(id);
//                review.setProduct(product);
//
//                // Lưu đánh giá vào cơ sở dữ liệu
//                reviewService.addReview(review);
//
//                return "redirect:/product-details/" + id;
//            } else {
//                return "redirect:/login";  // Nếu không tìm thấy người dùng
//            }
//        } else {
//            return "redirect:/login";  // Nếu người dùng không đăng nhập
//        }
//    }
//
//}

@Controller
public class ProductDetailsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // Hiển thị chi tiết sản phẩm và các đánh giá
    @GetMapping("/product-details/{id}")
    public String showProductDetails(@PathVariable("id") int id, Model model, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        Product product = productService.getProductById(id);
        Long reviewCount = reviewService.getReviewCountByProductId(id);
        Double avgRating = reviewService.getAverageRatingByProductId(id);

        if (avgRating == null || avgRating == 0.0) {
            avgRating = 5.0;
        }

        var reviews = reviewService.getApprovedReviewsByProductId(id);

        // Truyền thông tin người dùng vào model
        model.addAttribute("username", username != null ? username : "Guest");

        model.addAttribute("product", product);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviews", reviews);
        model.addAttribute("review", new Review());
        System.out.println("username1: " + username);
        return "product-details";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/product-details/{id}/add-review")
    public String addReview(@PathVariable("id") int id,
            @RequestParam("rating") int rating,
            @RequestParam("comment") String comment,
            Authentication authentication,
            Model model) {

        if (authentication != null) {
            String email = authentication.getName(); // Sử dụng email thay vì username
            System.out.println("Logged in user: " + email); // Kiểm tra email

            // Thay vì tìm theo username, tìm theo email
            Optional<User> optionalUser = userService.findByEmail(email);
            if (!optionalUser.isPresent()) {
                System.out.println("User not found in database");
                return "redirect:/login";
            }
            User user = optionalUser.get();

            // Tạo đối tượng review
            Review review = new Review();
            review.setRating(rating);
            review.setComment(comment);
            review.setUser(user);
            review.setCreatedAt(LocalDateTime.now());

            // Lấy sản phẩm và gán review cho sản phẩm
            Product product = productService.getProductById(id);
            review.setProduct(product);

            // Lưu đánh giá vào cơ sở dữ liệu
            reviewService.addReview(review);

            return "redirect:/product-details/" + id; // Quay lại trang chi tiết sản phẩm
        } else {
            return "redirect:/login"; // Nếu người dùng không đăng nhập
        }
    }

}
