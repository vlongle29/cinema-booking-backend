package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.seat.*;
import com.example.CineBook.service.SeatHoldService;
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
    private final SeatHoldService seatHoldService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo nhiều ghế cùng lúc")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> createSeats(@Valid @RequestBody CreateSeatsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seatService.createSeats(request)));
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách ghế theo phòng (Dùng cho admin quản lý phòng, không có thông tin trọng thái theo suất)")
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

    @PostMapping("/check-availability")
    @Operation(
        summary = "Kiểm tra ghế có available không (Bước 1: User chọn ghế)",
        description = "Check ghế có bị hold hoặc booked chưa. Gọi khi user click chọn ghế để validate real-time."
    )
    public ResponseEntity<ApiResponse<SeatAvailabilityResponse>> checkAvailability(
            @Valid @RequestBody SeatAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seatHoldService.checkAvailability(request)));
    }

    @PostMapping("/pre-hold")
    @Operation(
        summary = "Hold ghế tạm thời trước khi sang trang thanh toán (Bước 2: User bấm 'Tiếp tục')",
        description = "Tạo draft booking và hold ghế trong 5 phút. Gọi khi user bấm nút 'Tiếp tục' để sang trang thanh toán."
    )
    public ResponseEntity<ApiResponse<SeatPreHoldResponse>> preHoldSeats(
            @Valid @RequestBody SeatPreHoldRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            "Đã giữ ghế tạm thời", 
            seatHoldService.preHoldSeats(request)
        ));
    }
}
