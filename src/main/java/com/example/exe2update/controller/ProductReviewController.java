package com.example.exe2update.controller;

import com.example.exe2update.entity.Review;
import com.example.exe2update.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/productreview")
public class ProductReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public String getProductReview(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        List<Review> reviews = reviewRepository.findAll();
        model.addAttribute("reviews", reviews);
        return "productreview";
    }

    @PostMapping("/togglePublish")
    public String togglePublish(@RequestParam("reviewId") Integer reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setIsApproved(!review.getIsApproved());
            reviewRepository.save(review);
        }
        return "redirect:/productreview";
    }
}
