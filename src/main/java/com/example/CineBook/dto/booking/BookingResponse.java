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
public class BookingResponse {
    private UUID id;
    private UUID customerId;
    private UUID staffId;
    private UUID showtimeId;
    private UUID promotionId;
    private BigDecimal totalTicketPrice;
    private BigDecimal totalFoodPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime bookingDate;
    private LocalDateTime expiredAt;
    private BookingStatus status;
    private PaymentMethod paymentMethod;
}
