package com.example.CineBook.dto.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransactionRequest {
    
    @NotNull(message = "Booking ID không được để trống")
    private UUID bookingId;
    
    private String paymentGateway;
}
