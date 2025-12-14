package com.example.CineBook.dto.movie;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateMovieRequest {
    @NotBlank(message = "Tiêu đề phim không được để trống")
    private String title;
    
    private String description;
    private String director;
    private String cast;
    
    @NotNull(message = "Thời lượng phim không được để trống")
    @Min(value = 1, message = "Thời lượng phim phải lớn hơn 0")
    private Integer durationMinutes;
    
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String language;
    private String rated;
    
    @NotNull(message = "Trạng thái phim không được để trống")
    private String status;
    
    @NotEmpty(message = "Phim phải có ít nhất 1 thể loại")
    private List<UUID> genreIds;
}
