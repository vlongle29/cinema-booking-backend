package com.example.CineBook.service;


import com.example.CineBook.dto.review.CreateReviewRequest;
import com.example.CineBook.dto.review.MovieRatingSummaryResponse;
import com.example.CineBook.dto.review.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest request);
    
    Page<ReviewResponse> getReviewsByMovieId(UUID movieId, Pageable pageable);
    
    void toggleLike(UUID reviewId);

    MovieRatingSummaryResponse getMovieRatingSummary(UUID movieId);
}
