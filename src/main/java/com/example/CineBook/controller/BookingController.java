package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.booking.*;
import com.example.CineBook.dto.bookingproduct.BookingProductBatchRequest;
import com.example.CineBook.dto.ticket.TicketBatchRequest;
import com.example.CineBook.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs quản lý đặt vé")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Tạo booking mới (Legacy - sẽ deprecated)")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo booking thành công", bookingService.createBooking(request)));
    }

    @PostMapping("/draft")
    @Operation(summary = "Tạo draft booking (Bước 1: Chọn suất chiếu)")
    public ResponseEntity<ApiResponse<BookingResponse>> createDraftBooking(@Valid @RequestBody BookingDraftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo draft booking thành công", bookingService.createDraftBooking(request)));
    }

    @PostMapping("/{bookingId}/tickets/batch")
    @Operation(summary = "Thêm nhiều vé vào booking (Bước 2: Chọn ghế)")
    public ResponseEntity<ApiResponse<BookingResponse>> addTicketsBatch(
            @PathVariable UUID bookingId,
            @Valid @RequestBody TicketBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm vé thành công", bookingService.addTicketsBatch(bookingId, request)));
    }

    @PostMapping("/{bookingId}/products/batch")
    @Operation(summary = "Thêm nhiều sản phẩm vào booking (Bước 3: Chọn đồ ăn)")
    public ResponseEntity<ApiResponse<BookingResponse>> addProductsBatch(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingProductBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm thành công", bookingService.addProductsBatch(bookingId, request)));
    }

    @GetMapping("/{bookingId}/summary")
    @Operation(summary = "Lấy tổng kết booking (Hiển thị trước khi thanh toán)")
    public ResponseEntity<ApiResponse<BookingSummaryResponse>> getBookingSummary(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingSummary(bookingId)));
    }

    @PutMapping("/{bookingId}/checkout")
    @Operation(summary = "Thanh toán booking (Bước 4: Checkout)")
    public ResponseEntity<ApiResponse<BookingResponse>> checkout(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingCheckoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Checkout thành công", bookingService.checkout(bookingId, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin booking theo ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(id)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy danh sách booking theo user ID")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingsByUserId(userId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa booking")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa booking thành công", null));
    }
}
