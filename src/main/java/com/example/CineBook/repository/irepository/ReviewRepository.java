package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Review;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByMovieId(UUID movieId, Pageable pageable);

    Boolean existsByUserIdAndMovieId(UUID userId, UUID movieId);

    Integer countLikesById(UUID reviewId);

    // Đếm tổng số đánh giá của một bộ phim
    Integer countByMovieId(UUID movieId);

    // Tính điểm trung bình của một bộ phim
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.movieId = :movieId")
    Double getAverageRatingByMovieId(@Param("movieId") UUID movieId);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void incrementLikeCount(@Param("reviewId") UUID reviewId);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = CASE WHEN r.likeCount > 0 THEN r.likeCount - 1 ELSE 0 END WHERE r.id = :reviewId")
    void decrementLikeCount(@Param("reviewId") UUID reviewId);
}
