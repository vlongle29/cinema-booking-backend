package com.example.CineBook.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingDraftRequest {
    @NotNull(message = "Showtime ID không được để trống")
    private UUID showtimeId;
}
