package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.seat.SeatResponse;
import com.example.CineBook.dto.seattemplate.*;
import com.example.CineBook.service.SeatTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seat-templates")
@RequiredArgsConstructor
@Tag(name = "Seat Template", description = "API quản lý mẫu ghế ngồi")
public class SeatTemplateController {

    private final SeatTemplateService seatTemplateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo mẫu ghế ngồi", description = "Tạo mẫu ghế ngồi cho phòng chiếu")
    public ResponseEntity<ApiResponse<SeatTemplateResponse>> createSeatTemplate(
            @Valid @RequestBody CreateSeatTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seatTemplateService.createTemplate(request)));
    }

    @PostMapping("/{id}/seats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Thêm ghế vào mẫu", description = "Thêm ghế vào mẫu ghế ngồi đã tạo")
    public ResponseEntity<ApiResponse<SeatTemplateResponse>> addSeatsToTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody AddSeatsToTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seatTemplateService.addSeatsToTemplate(id, request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Lấy danh sách mẫu ghế ngồi", description = "Lấy tất cả mẫu ghế ngồi đã tạo")
    public ResponseEntity<ApiResponse<List<SeatTemplateResponse>>> getSeatTemplates() {
        return ResponseEntity.ok(ApiResponse.success(seatTemplateService.getAllTemplates()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Lấy chi tiết mẫu ghế ngồi", description = "Lấy chi tiết mẫu ghế ngồi theo ID")
    public ResponseEntity<ApiResponse<SeatTemplateResponse>> getSeatTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seatTemplateService.getTemplateById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa mẫu ghế ngồi", description = "Xóa mẫu ghế ngồi theo ID")
    public ResponseEntity<ApiResponse<Void>> deleteSeatTemplate(@PathVariable UUID id) {
        seatTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Áp dụng mẫu ghế vào phòng", description = "Clone tất cả ghế từ template vào phòng chiếu")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> applyTemplateToRoom(
            @Valid @RequestBody ApplyTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                seatTemplateService.applyTemplateToRoom(request.getTemplateId(), request.getRoomId())));
    }
}
