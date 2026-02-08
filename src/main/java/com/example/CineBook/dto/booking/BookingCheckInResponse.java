package com.example.CineBook.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCheckInResponse {
    private UUID bookingId;
    private String bookingCode;
    private String movieTitle;
    private LocalDateTime showtimeStartTime;
    private String branchName;
    private String roomName;
    private List<String> seatNames;
    private Integer ticketCount;
    private BigDecimal totalAmount;
    private Boolean isCheckedIn;
    private Instant checkedInAt;
}
