package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
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
public class Review extends AuditingEntity {
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "movie_id", nullable = false)
    private UUID movieId;
    
    @Column
    private Integer rating;
    
    @Column(columnDefinition = "TEXT")
    private String comment;
}
