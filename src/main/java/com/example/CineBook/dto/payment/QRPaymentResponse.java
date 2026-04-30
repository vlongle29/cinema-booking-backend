package com.example.CineBook.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRPaymentResponse {
    private UUID bookingId;
    private String qrCodeData; // QR code string để FE render
    private String qrCodeImageBase64; // Hoặc trả về ảnh Base64 luôn
    private BigDecimal amount;
    private String transactionRef;
    private Integer expiresInSeconds; // QR code hết hạn sau bao lâu
}
