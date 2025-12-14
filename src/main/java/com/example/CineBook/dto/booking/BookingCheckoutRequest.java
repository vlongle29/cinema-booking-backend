package com.example.CineBook.dto.booking;

import com.example.CineBook.common.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingCheckoutRequest {
    private UUID promotionId;
    private UUID giftCardId;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
}
