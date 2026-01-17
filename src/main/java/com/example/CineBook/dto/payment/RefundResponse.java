package com.example.CineBook.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private UUID bookingId;
    private BigDecimal refundAmount;
    private String status;
    private String message;
    private LocalDateTime refundDate;
    private String transactionNo;
}
