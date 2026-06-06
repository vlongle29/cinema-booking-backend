package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.AgeRating;
import com.example.CineBook.common.constant.MovieStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie extends AuditingEntity {
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column
    private String director;
    
    @Column(name = "movie_cast", columnDefinition = "TEXT")
    private String cast;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    @Column(name = "poster_url")
    private String posterUrl;
    
    @Column(name = "trailer_url")
    private String trailerUrl;
    
    @Column
    private String language;
    
    @Enumerated(EnumType.STRING)
    @Column
    private AgeRating rated;
    
    @Enumerated(EnumType.STRING)
    @Column
    private MovieStatus status;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
}
