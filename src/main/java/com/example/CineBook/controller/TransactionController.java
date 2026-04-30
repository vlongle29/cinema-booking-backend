package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.transaction.TransactionRequest;
import com.example.CineBook.dto.transaction.TransactionResponse;
import com.example.CineBook.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Transaction Management", description = "APIs quản lý giao dịch thanh toán")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/process")
    @Operation(summary = "Xử lý thanh toán (fake payment)")
    public ResponseEntity<ApiResponse<TransactionResponse>> processPayment(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.processPayment(request)));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Lấy thông tin giao dịch theo booking ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getByBookingId(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getByBookingId(bookingId)));
    }
}
