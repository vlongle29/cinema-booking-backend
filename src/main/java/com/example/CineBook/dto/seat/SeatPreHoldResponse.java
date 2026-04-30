package com.example.CineBook.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatPreHoldResponse {
    
    private UUID tempBookingId;
    private UUID showtimeId;
    private List<UUID> heldSeatIds;
    private LocalDateTime expiresAt;
    private String message;
}
