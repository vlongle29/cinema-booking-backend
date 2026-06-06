package com.example.CineBook.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID movieId;
    private UUID userId;
    private String username;
    private String userAvatar;
    private Integer rating;
    private String content;
    private Boolean isVerified;
    private Boolean isSpoiler;
    private Boolean isLikedByCurrentUser;
    private Integer likeCount;
    private Instant createdAt;
    private Instant updatedAt;
}
