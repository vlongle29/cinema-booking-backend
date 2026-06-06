package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.model.auditing.AuditingEntityListener;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Review extends AuditingEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "movie_id", nullable = false)
    private UUID movieId;
    
    @Column
    private Integer rating; // Đánh giá từ 1 đến 5

    @Column(columnDefinition = "TEXT")
    private String content; // Nội dung đánh giá

    @Column(name = "like_count", nullable = false)
    private Integer likeCount; // Số lượt thích của đánh giá

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified; // True nếu đánh giá đã được xác thực là từ người dùng đã xem phim, false nếu chưa xác thực hoặc không xác thực được.

    @Column(name = "is_spoiler", nullable = false)
    private Boolean isSpoiler; // True nếu user đánh dấu có tiết lộ nội dung.
}
