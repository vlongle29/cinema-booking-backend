package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.seat.SeatHoldRequest;
import com.example.CineBook.service.SeatHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/{bookingId}/seats")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<Void>> holdSeats(
            @PathVariable UUID bookingId,
            @RequestBody SeatHoldRequest request) {
        seatHoldService.holdSeats(bookingId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/release/{showtimeId}/{seatId}")
    public ResponseEntity<ApiResponse<Void>> releaseSeat(
            @PathVariable UUID bookingId,
            @PathVariable UUID showtimeId,
            @PathVariable UUID seatId) {
        seatHoldService.releaseSeat(bookingId, showtimeId, seatId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/release-all")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(@PathVariable UUID bookingId) {
        seatHoldService.releaseSeats(bookingId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
