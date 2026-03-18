package com.example.CineBook.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponse {
    private UUID id;
    private String ticketCode;
    private UUID bookingId;
    private UUID seatId;
    private String seatNumber;
    private UUID showtimeId;
    private String movieTitle;
    private String cinemaName;
    private LocalDateTime showtime;
    private BigDecimal price;
    private String qrCodeBase64;
    private Boolean isCheckedIn;
    private Instant createdAt;
}
