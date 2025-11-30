package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.seat.CreateSeatsRequest;
import com.example.CineBook.dto.seat.SeatResponse;
import com.example.CineBook.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Seat Management", description = "APIs quản lý ghế ngồi")
@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo nhiều ghế cùng lúc")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> createSeats(@Valid @RequestBody CreateSeatsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seatService.createSeats(request)));
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách ghế theo phòng")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getSeatsByRoom(roomId)));
    }

    @DeleteMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa tất cả ghế của phòng", description = "Dùng khi thay đổi layout phòng")
    public ResponseEntity<ApiResponse<Void>> deleteAllSeatsByRoom(@PathVariable UUID roomId) {
        seatService.deleteAllSeatsByRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
