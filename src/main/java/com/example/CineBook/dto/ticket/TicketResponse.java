package com.example.CineBook.dto.ticket;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TicketResponse {
    private UUID id;
    private UUID bookingId;
    private UUID seatId;
    private UUID showtimeId;
    private BigDecimal price;
    private UUID createdBy;
    private Instant createTime;
}
