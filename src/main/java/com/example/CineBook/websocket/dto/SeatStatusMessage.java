package com.example.CineBook.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusMessage {
    private String type; // "SEAT_SELECTED", "SEAT_BOOKED", "SEAT_RELEASED"
    private UUID showtimeId;
    private UUID seatId;
    private UUID bookingId;
    private LocalDateTime expiresAt;
}
