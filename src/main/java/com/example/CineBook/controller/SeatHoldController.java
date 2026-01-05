package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.seat.SeatHoldRequest;
import com.example.CineBook.service.SeatHoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/{bookingId}/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Hold", description = "API quản lý giữ chỗ ghế ngồi")
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @PostMapping("/hold")
    @Operation(summary = "Giữ ghế", description = "Giữ ghế cho booking trong thời gian nhất định")
    public ResponseEntity<ApiResponse<Void>> holdSeats(
            @Parameter(description = "ID của booking") @PathVariable UUID bookingId,
            @RequestBody SeatHoldRequest request) {
        seatHoldService.holdSeats(bookingId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/release/{showtimeId}/{seatId}")
    @Operation(summary = "Hủy giữ ghế", description = "Hủy giữ một ghế cụ thể")
    public ResponseEntity<ApiResponse<Void>> releaseSeat(
            @Parameter(description = "ID của booking") @PathVariable UUID bookingId,
            @Parameter(description = "ID của suất chiếu") @PathVariable UUID showtimeId,
            @Parameter(description = "ID của ghế") @PathVariable UUID seatId) {
        seatHoldService.releaseSeat(bookingId, showtimeId, seatId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/release-all")
    @Operation(summary = "Hủy tất cả ghế đã giữ", description = "Hủy tất cả ghế đã giữ của booking")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @Parameter(description = "ID của booking") @PathVariable UUID bookingId) {
        seatHoldService.releaseSeats(bookingId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
