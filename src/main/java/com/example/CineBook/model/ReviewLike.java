package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "review_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "review_id"})
})
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike extends AuditingEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;
}
