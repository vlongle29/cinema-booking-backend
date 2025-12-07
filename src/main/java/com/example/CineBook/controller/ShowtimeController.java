package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.showtime.CreateShowtimeRequest;
import com.example.CineBook.dto.showtime.ShowtimeResponse;
import com.example.CineBook.dto.showtime.ShowtimeSearchDTO;
import com.example.CineBook.dto.showtime.UpdateShowtimeRequest;
import com.example.CineBook.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Showtime Management", description = "APIs quản lý suất chiếu phim")
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {
    
    private final ShowtimeService showtimeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo suất chiếu mới")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> createShowtime(
            @Valid @RequestBody CreateShowtimeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(showtimeService.createShowtime(request)));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm suất chiếu")
    public ResponseEntity<ApiResponse<PageResponse<ShowtimeResponse>>> searchShowtimes(
            @ModelAttribute ShowtimeSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(showtimeService.searchShowtimes(searchDTO)));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết suất chiếu")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> getShowtimeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(showtimeService.getShowtimeById(id)));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật suất chiếu")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> updateShowtime(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShowtimeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(showtimeService.updateShowtime(id, request)));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa suất chiếu")
    public ResponseEntity<ApiResponse<Void>> deleteShowtime(@PathVariable UUID id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
