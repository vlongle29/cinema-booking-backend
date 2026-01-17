package com.example.CineBook.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    public String paymentUrl;
}
