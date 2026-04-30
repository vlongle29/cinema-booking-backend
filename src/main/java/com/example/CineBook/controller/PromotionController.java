package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.promotion.PromotionRequest;
import com.example.CineBook.dto.promotion.PromotionResponse;
import com.example.CineBook.dto.promotion.PromotionSearchDTO;
import com.example.CineBook.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Promotion Management", description = "APIs quản lý khuyến mãi")
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo khuyến mãi mới")
    public ResponseEntity<ApiResponse<PromotionResponse>> create(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm khuyến mãi")
    public ResponseEntity<ApiResponse<PageResponse<PromotionResponse>>> search(@ModelAttribute PromotionSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.search(searchDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa khuyến mãi (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        promotionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
