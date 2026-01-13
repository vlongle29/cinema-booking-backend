package com.example.CineBook.dto.booking;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.constant.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MyBookingResponse {
    private UUID id;
    private UUID showtimeId;
    private LocalDateTime showtimeStartTime;
    private String movieTitle;
    private String moviePosterUrl;
    private String branchName;
    private String roomName;
    private String cityName;
    private Integer ticketCount;
    private BigDecimal totalTicketPrice;
    private BigDecimal totalFoodPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime bookingDate;
    private BookingStatus status;
    private PaymentMethod paymentMethod;
}
