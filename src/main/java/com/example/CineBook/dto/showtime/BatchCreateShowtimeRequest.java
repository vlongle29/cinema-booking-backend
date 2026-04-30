package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateShowtimeRequest {

    @NotNull(message = "Movie ID is required")
    private UUID movieId;
    
    @NotNull(message = "Room ID is required")
    private UUID roomId;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @NotEmpty(message = "Times list cannot be empty")
    private List<String> times;
    
    @NotNull(message = "Format is required")
    private MovieFormat format;
}
