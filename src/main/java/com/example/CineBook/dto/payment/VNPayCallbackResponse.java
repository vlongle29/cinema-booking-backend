package com.example.CineBook.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class VNPayCallbackResponse {
    private UUID bookingId;
    private String status;
    private String message;
}
