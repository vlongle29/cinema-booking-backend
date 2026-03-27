package com.example.CineBook.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRefreshRequest {
    @NotNull(message = "Showtime ID không được để trống")
    private UUID showtimeId;
}
