package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.room.RoomRequest;
import com.example.CineBook.dto.room.RoomResponse;
import com.example.CineBook.dto.room.RoomSearchDTO;
import com.example.CineBook.service.RoomService;
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

@Tag(name = "Room Management", description = "APIs quản lý phòng chiếu")
@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ShowtimeService showtimeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo phòng chiếu mới")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roomService.createRoom(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách tất cả phòng chiếu", description = "Super Admin: lấy tất cả. Branch user: chỉ lấy phòng của chi nhánh")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.success(roomService.getAllRooms()));
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Xem thông tin phòng chiếu")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(roomService.getRoomById(id)));
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật thông tin phòng chiếu")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roomService.updateRoom(id, request)));
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa mềm phòng chiếu", description = "Chỉ xóa nếu không có ghế nào")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable UUID id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}/cascade")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa phòng và tất cả ghế", description = "Xóa cascade: Room → Seats")
    public ResponseEntity<ApiResponse<Void>> deleteRoomCascade(@PathVariable UUID id) {
        roomService.deleteRoomCascade(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}/restore")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Khôi phục phòng đã xóa mềm")
    public ResponseEntity<ApiResponse<RoomResponse>> restoreRoom(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(roomService.restoreRoom(id)));
    }

    @GetMapping("/search")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Tìm kiếm và lọc phòng chiếu")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> searchRooms(
            @ModelAttribute RoomSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(roomService.searchRooms(searchDTO)));
    }
    
    @GetMapping("/{roomId}/available-slots")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Lấy danh sách time slots khả dụng cho phòng và phim trong ngày")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableTimeSlots(
            @PathVariable UUID roomId,
            @RequestParam UUID movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
            showtimeService.getAvailableTimeSlots(roomId, movieId, date)));
    }
}
