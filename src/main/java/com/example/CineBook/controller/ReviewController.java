package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.review.CreateReviewRequest;
import com.example.CineBook.dto.review.ReviewResponse;
import com.example.CineBook.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Review", description = "APIs liên quan đến đánh giá phim")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN', 'CUSTOMER')")
    @Operation(summary = "Tạo đánh giá mới", description = "Cho phép người dùng tạo đánh giá cho phim đã xem")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.createReview(request)));
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Lấy danh sách đánh giá theo phim", description = "Lấy danh sách đánh giá của một bộ phim có phân trang")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByMovieId(
            @PathVariable UUID movieId,
            @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByMovieId(movieId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @PostMapping("/{reviewId}/like")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN', 'CUSTOMER')")
    @Operation(summary = "Toggle like bình luận", description = "Thích hoặc bỏ thích một đánh giá")
    public ResponseEntity<ApiResponse<Void>> toggleLike(@PathVariable UUID reviewId) {
        reviewService.toggleLike(reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
