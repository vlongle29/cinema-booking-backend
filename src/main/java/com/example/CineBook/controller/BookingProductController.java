package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.bookingproduct.BookingProductCreateRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductResponse;
import com.example.CineBook.service.BookingProductService;
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
@RequestMapping("/api/booking-products")
@RequiredArgsConstructor
@Tag(name = "Booking Product Management", description = "APIs quản lý sản phẩm trong booking")
public class BookingProductController {

    private final BookingProductService bookingProductService;

    @PostMapping
    @Operation(summary = "Thêm sản phẩm vào booking")
    public ResponseEntity<ApiResponse<BookingProductResponse>> createBookingProduct(@Valid @RequestBody BookingProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm sản phẩm vào booking thành công", bookingProductService.createBookingProduct(request)));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Lấy danh sách sản phẩm theo booking ID")
    public ResponseEntity<ApiResponse<List<BookingProductResponse>>> getBookingProductsByBookingId(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(bookingProductService.getBookingProductsByBookingId(bookingId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm khỏi booking")
    public ResponseEntity<ApiResponse<Void>> deleteBookingProduct(@PathVariable UUID id) {
        bookingProductService.deleteBookingProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi booking thành công", null));
    }
}
