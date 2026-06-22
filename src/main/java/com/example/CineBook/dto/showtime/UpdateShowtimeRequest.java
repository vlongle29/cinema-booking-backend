package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.common.constant.ShowtimeStatus;
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
public class UpdateShowtimeRequest {
    private UUID movieId;
    private UUID roomId;
    private LocalDateTime startTime;
    
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;
    
    private MovieFormat format;
    
    private ShowtimeStatus status;
}
