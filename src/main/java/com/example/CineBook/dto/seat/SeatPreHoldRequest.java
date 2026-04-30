package com.example.CineBook.dto.seat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatPreHoldRequest {
    
    @NotNull(message = "Showtime ID không được để trống")
    private UUID showtimeId;
    
    @NotEmpty(message = "Danh sách ghế không được để trống")
    private List<UUID> seatIds;
}
