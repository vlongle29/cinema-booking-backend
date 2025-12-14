package com.example.CineBook.dto.booking;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingCreateRequest {
    private UUID customerId;
    private UUID staffId;
    
    @NotNull(message = "Showtime ID không được để trống")
    private UUID showtimeId;
    
    private UUID promotionId;
    private BigDecimal totalTicketPrice;
    private BigDecimal totalFoodPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BookingStatus status;
    private PaymentMethod paymentMethod;
}
