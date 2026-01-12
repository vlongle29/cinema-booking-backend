package com.example.CineBook.controller;

import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.city.CityResponse;
import com.example.CineBook.dto.showtime.*;
import com.example.CineBook.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
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
    
    @GetMapping("/available-dates")
    @Operation(summary = "Lấy danh sách ngày có suất chiếu của phim")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(
            @RequestParam UUID movieId) {
        return ResponseEntity.ok(ApiResponse.success(showtimeService.getAvailableDates(movieId)));
    }
    
    @GetMapping("/available-cities")
    @Operation(summary = "Lấy danh sách thành phố có suất chiếu theo phim và ngày")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAvailableCities(
            @RequestParam UUID movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(showtimeService.getAvailableCities(movieId, date)));
    }
    
    @GetMapping("/available-formats")
    @Operation(summary = "Lấy danh sách format có sẵn theo phim, ngày và thành phố")
    public ResponseEntity<ApiResponse<List<MovieFormat>>> getAvailableFormats(
            @RequestParam UUID movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam UUID cityId) {
        return ResponseEntity.ok(ApiResponse.success(showtimeService.getAvailableFormats(movieId, date, cityId)));
    }
    
    @GetMapping("/grouped-by-branch")
    @Operation(summary = "Lấy danh sách suất chiếu nhóm theo chi nhánh")
    public ResponseEntity<ApiResponse<List<ShowtimeGroupedByBranchResponse>>> getShowtimesGroupedByBranch(
            @RequestParam UUID movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam UUID cityId,
            @RequestParam(required = false) MovieFormat format) {
        return ResponseEntity.ok(ApiResponse.success(
            showtimeService.getShowtimesGroupedByBranch(movieId, date, cityId, format)));
    }
    
    @GetMapping("/{showtimeId}/seats")
    @Operation(summary = "Lấy trạng thái ghế theo suất chiếu", 
               description = "Hiển thị tất cả ghế với trạng thái: AVAILABLE, BOOKED, HELD")
    public ResponseEntity<ApiResponse<ShowtimeSeatStatusResponse>> getSeatStatusByShowtime(
            @PathVariable UUID showtimeId) {
        return ResponseEntity.ok(ApiResponse.success(
            showtimeService.getSeatStatusByShowtime(showtimeId)));
    }
    
    @GetMapping("/by-room")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Lấy danh sách suất chiếu theo phòng và khoảng ngày")
    public ResponseEntity<ApiResponse<RoomShowtimeResponse>> getShowtimesByRoom(
            @RequestParam UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
            showtimeService.getShowtimesByRoom(roomId, startDate, endDate)));
    }
    
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo hàng loạt suất chiếu")
    public ResponseEntity<ApiResponse<BulkCreateShowtimeResponse>> bulkCreateShowtimes(
            @Valid @RequestBody BulkCreateShowtimeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            showtimeService.bulkCreateShowtimes(request)));
    }
}
