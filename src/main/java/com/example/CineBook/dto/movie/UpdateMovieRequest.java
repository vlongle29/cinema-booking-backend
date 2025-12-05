package com.example.CineBook.dto.movie;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateMovieRequest {
    private String title;
    private String description;
    private String director;
    private String cast;
    
    @Min(value = 1, message = "Thời lượng phim phải lớn hơn 0")
    private Integer durationMinutes;
    
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String language;
    private String rated;
    private String status;
    private List<UUID> genreIds;
}
