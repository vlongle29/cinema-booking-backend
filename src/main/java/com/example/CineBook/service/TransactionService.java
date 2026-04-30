package com.example.CineBook.service;

import com.example.CineBook.dto.transaction.TransactionRequest;
import com.example.CineBook.dto.transaction.TransactionResponse;

import java.util.UUID;

public interface TransactionService {
    TransactionResponse processPayment(TransactionRequest request);
    TransactionResponse getByBookingId(UUID bookingId);
}
