package com.example.exe2update.service.impl;

import com.example.exe2update.entity.Review;
import com.example.exe2update.repository.ReviewRepository;
import com.example.exe2update.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public List<Review> getApprovedReviewsByProductId(int productId) {
        return reviewRepository.findByProduct_ProductIdAndIsApprovedTrue(productId);
    }

    @Override
    public Double getAverageRatingByProductId(Integer productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public Long getReviewCountByProductId(Integer productId) {
        Long count = reviewRepository.countReviewsByProductId(productId);
        return count != null ? count : 0L;
    }


    @Override
    public List<Review> getReviewsByProductId(int productId) {
        return reviewRepository.findByProduct_ProductId(productId);
    }

    @Override
    public void addReview(Review review) {
        reviewRepository.save(review);
    }
}
