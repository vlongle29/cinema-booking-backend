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
    private String seatName;
    private String seatTypeName;
    private UUID showtimeId;
    private BigDecimal price;
    private String ticketCode;
    private UUID createdBy;
    private Instant createTime;
}
