package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowtimeRequest {
    
    @NotNull(message = "Movie ID is required")
    private UUID movieId;
    
    @NotNull(message = "Room ID is required")
    private UUID roomId;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Format is required")
    private MovieFormat format;
}
