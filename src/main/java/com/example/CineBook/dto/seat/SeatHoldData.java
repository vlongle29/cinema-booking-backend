package com.example.CineBook.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldData implements Serializable {
    private UUID seatId;
    private UUID showtimeId;
    private UUID bookingId;
    private LocalDateTime heldAt;
    private LocalDateTime expiresAt;
}
