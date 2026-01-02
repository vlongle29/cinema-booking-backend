package com.example.CineBook.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldRequest {
    private UUID showtimeId;
    private UUID seatId; // Single seat per request
}
