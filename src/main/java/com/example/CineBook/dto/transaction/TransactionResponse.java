package com.example.CineBook.dto.transaction;

import com.example.CineBook.common.constant.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponse {
    private UUID id;
    private UUID bookingId;
    private String transactionCode;
    private String paymentGateway;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime transactionTime;
    private LocalDateTime createdAt;
}
