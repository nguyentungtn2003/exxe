package com.example.exe2update.service;

import com.example.exe2update.entity.Review;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReviewService {
    Double getAverageRatingByProductId(Integer productId);
    Long getReviewCountByProductId(Integer productId);
    List<Review> getReviewsByProductId(int productId);
    void addReview(Review review);
    List<Review> getApprovedReviewsByProductId(int productId);

}