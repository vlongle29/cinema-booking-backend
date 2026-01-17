package com.example.CineBook.service;

import com.example.CineBook.dto.payment.PaymentResponse;
import com.example.CineBook.dto.payment.RefundResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse createVNPayPayment(HttpServletRequest request);
    void handleVNPayCallback(UUID bookingId, String responseCode, String transactionNo);
    RefundResponse refundBooking(UUID bookingId, String reason);
}
