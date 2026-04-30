package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.booking.*;
import com.example.CineBook.dto.bookingproduct.BookingProductBatchRequest;
import com.example.CineBook.dto.ticket.TicketBatchRequest;
import com.example.CineBook.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

//    @PostMapping
//    @Operation(summary = "Tạo booking mới (Legacy - sẽ deprecated)")
//    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingCreateRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success("Tạo booking thành công", bookingService.createBooking(request)));
//    }

    @PostMapping("/confirm")
    @Operation(summary = "Xác nhận đặt vé (Tạo booking + Hold ghế + Lưu sản phẩm)",
               description = "User chọn ghế và sản phẩm xong, click 'Đặt vé' → Gọi API này → Bắt đầu countdown 15 phút")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@Valid @RequestBody BookingConfirmRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Xác nhận đặt vé thành công. Vui lòng thanh toán trong 15 phút.", 
                    bookingService.confirmBooking(request)));
    }


    @Deprecated(since = "2.0", forRemoval = true)
    @PostMapping("/draft")
    @Operation(
        summary = "[DEPRECATED] Tạo draft booking - Sử dụng POST /api/booking/confirm thay thế",
        description = "⚠️ API này đã deprecated kể từ version 2.0. " +
                      "Vui lòng sử dụng POST /api/booking/confirm để tạo booking và hold ghế cùng lúc. " +
                      "API này sẽ bị xóa trong version 3.0.",
        deprecated = true
    )
    public ResponseEntity<ApiResponse<BookingResponse>> createDraftBooking(@Valid @RequestBody BookingDraftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo draft booking thành công", bookingService.createDraftBooking(request)));
    }

    @PostMapping("/{bookingId}/refresh")
    @Operation(summary = "Làm mới hoặc tạo lại booking khi hết hạn", 
               description = "Gia hạn booking nếu còn DRAFT, hoặc tạo booking mới nếu đã EXPIRED")
    public ResponseEntity<ApiResponse<BookingResponse>> refreshOrCreateBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Làm mới booking thành công", 
            bookingService.refreshOrCreateBooking(bookingId)));
    }

    @Deprecated(since = "2.0", forRemoval = true)
    @PostMapping("/{bookingId}/tickets/batch")
    @Operation(
        summary = "[DEPRECATED] Thêm vé - API này không còn được sử dụng",
        description = "⚠️ API này đã deprecated kể từ version 2.0. " +
                      "Vé sẽ được tạo tự động trong quá trình checkout từ Redis holds. " +
                      "Vui lòng sử dụng POST /api/booking/confirm để hold ghế. " +
                      "API này sẽ bị xóa trong version 3.0.",
        deprecated = true
    )
    public ResponseEntity<ApiResponse<BookingResponse>> addTicketsBatch(
            @PathVariable UUID bookingId,
            @Valid @RequestBody TicketBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm vé thành công", bookingService.addTicketsBatch(bookingId, request)));
    }

    @PostMapping("/{bookingId}/products/batch")
    @Operation(
        summary = "Thêm sản phẩm vào booking đã tạo",
        description = "💡 Khuyến nghị: Gửi products cùng với POST /api/booking/confirm để giảm số lượng API calls. " +
                      "Tuy nhiên, API này vẫn có thể dùng để thêm products sau khi đã confirm booking."
    )
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

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Hủy booking", description = "User hủy booking trước khi suất chiếu bắt đầu")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Hủy booking thành công", 
            bookingService.cancelBooking(bookingId, reason)));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Lấy lịch sử đặt vé của user hiện tại")
    public ResponseEntity<ApiResponse<PageResponse<MyBookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
        return ResponseEntity.ok(ApiResponse.success(bookingService.getMyBookings(pageable)));
    }

    @PostMapping("/check-in/{bookingCode}")
    @Operation(summary = "Check-in bằng booking code (Scan QR tại rạp)", 
               description = "Nhân viên scan QR code để check-in tất cả vé trong booking")
    public ResponseEntity<ApiResponse<BookingCheckInResponse>> checkInByBookingCode(@PathVariable String bookingCode) {
        return ResponseEntity.ok(ApiResponse.success("Check-in thành công", 
            bookingService.checkInByBookingCode(bookingCode)));
    }
}
