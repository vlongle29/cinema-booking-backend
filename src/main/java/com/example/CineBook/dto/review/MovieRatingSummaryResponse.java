package com.example.CineBook.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieRatingSummaryResponse {
    private Double averageRating;
    private Integer totalReviews;
    private List<RatingCountDTO> distribution; // Chứa mảng 5 phần tử cho 5 thanh gạch
}
