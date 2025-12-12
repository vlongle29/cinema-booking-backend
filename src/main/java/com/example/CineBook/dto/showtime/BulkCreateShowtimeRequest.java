package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateShowtimeRequest {
    
    @NotNull(message = "Movie ID is required")
    private UUID movieId;
    
    @NotNull(message = "Room ID is required")
    private UUID roomId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotEmpty(message = "Time slots cannot be empty")
    @Size(max = 10, message = "Maximum 10 time slots allowed")
    @JsonFormat(pattern = "HH:mm:ss")
    private List<LocalTime> timeSlots;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Format is required")
    private MovieFormat format;
}
