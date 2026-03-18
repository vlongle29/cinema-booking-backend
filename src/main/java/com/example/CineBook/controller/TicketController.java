package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.ticket.TicketCreateRequest;
import com.example.CineBook.dto.ticket.TicketDetailResponse;
import com.example.CineBook.dto.ticket.TicketResponse;
import com.example.CineBook.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Management", description = "APIs quản lý vé")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Deprecated
    @Operation(summary = "[DEPRECATED] Tạo ticket mới - Sử dụng booking check-in thay thế",
               description = "API này không còn được khuyến nghị. Sử dụng POST /api/booking/check-in/{bookingCode} để check-in toàn bộ booking")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo ticket thành công", ticketService.createTicket(request)));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Lấy danh sách ticket theo booking ID")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByBookingId(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketsByBookingId(bookingId)));
    }

    @GetMapping("/showtime/{showtimeId}")
    @Operation(summary = "Lấy danh sách ticket theo showtime ID")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByShowtimeId(@PathVariable UUID showtimeId) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketsByShowtimeId(showtimeId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa ticket")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ticket thành công", null));
    }
    
    @GetMapping("/code/{ticketCode}")
    @Deprecated
    @Operation(summary = "[DEPRECATED] Tra cứu vé bằng mã ticket code - Sử dụng booking lookup thay thế",
               description = "Sử dụng GET /api/booking/{bookingId} hoặc check-in endpoint để tra cứu thông tin")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketByCode(@PathVariable String ticketCode) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketByCode(ticketCode)));
    }
    
    @PostMapping("/check-in/{ticketCode}")
    @Deprecated
    @Operation(summary = "[DEPRECATED] Check-in vé đơn lẻ - Sử dụng booking check-in thay thế",
               description = "API này không còn được khuyến nghị. Sử dụng POST /api/booking/check-in/{bookingCode} để check-in toàn bộ booking")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> checkInTicket(@PathVariable String ticketCode) {
        return ResponseEntity.ok(ApiResponse.success("Check-in thành công", ticketService.checkInTicket(ticketCode)));
    }
    
    @GetMapping(value = "/qr/{ticketCode}", produces = MediaType.IMAGE_PNG_VALUE)
    @Deprecated
    @Operation(summary = "[DEPRECATED] Lấy ảnh QR code của vé đơn lẻ - Không còn sử dụng",
               description = "QR code hiện tại được gửi qua email dựa trên bookingCode, không phải ticketCode")
    public ResponseEntity<byte[]> getQRCode(@PathVariable String ticketCode) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(ticketService.getQRCodeImage(ticketCode));
    }
}
