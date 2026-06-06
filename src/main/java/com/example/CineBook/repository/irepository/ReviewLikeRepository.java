package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.ReviewLike;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {
    
    Optional<ReviewLike> findByUserIdAndReviewId(UUID userId, UUID reviewId);
    
    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);
    
    void deleteByUserIdAndReviewId(UUID userId, UUID reviewId);

    // Tìm xem trong danh sách reviewIds này, những review nào đã được user tương ứng liked
    @Query("SELECT rl.reviewId FROM ReviewLike rl WHERE rl.userId = :userId AND rl.reviewId IN :reviewIds")
    Set<UUID> findLikedReviewIdsByUserIdAndReviewIds(@Param("userId") UUID userId, @Param("reviewIds") Set<UUID> reviewIds);
}
