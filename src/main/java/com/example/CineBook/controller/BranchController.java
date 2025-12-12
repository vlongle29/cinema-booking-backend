package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.branch.BranchRequest;
import com.example.CineBook.dto.branch.BranchResponse;
import com.example.CineBook.dto.branch.BranchSearchDTO;
import com.example.CineBook.dto.room.RoomResponse;
import com.example.CineBook.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Branch Management", description = "APIs quản lý chi nhánh")
@RestController
@RequestMapping("/api/branch")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;
    private final com.example.CineBook.service.RoomService roomService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(summary = "Tạo chi nhánh mới")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.createBranch(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Xem thông tin chi nhánh")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật thông tin chi nhánh")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID id,
            @Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.updateBranch(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(summary = "Xóa mềm chi nhánh", description = "Chỉ xóa nếu không có phòng nào")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}/cascade")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(summary = "Xóa chi nhánh và tất cả phòng, ghế", description = "Xóa cascade: Branch → Rooms → Seats")
    public ResponseEntity<ApiResponse<Void>> deleteBranchCascade(@PathVariable UUID id) {
        branchService.deleteBranchCascade(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Tìm kiếm và lọc chi nhánh")
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> searchBranches(
            @ModelAttribute BranchSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(branchService.searchBranches(searchDTO)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Lấy tất cả chi nhánh")
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> getAllBranches(
            @ModelAttribute BranchSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getAllBranches(searchDTO)));
    }

    @GetMapping("/{branchId}/rooms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách phòng theo chi nhánh")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByBranch(@PathVariable UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(roomService.getRoomsByBranch(branchId)));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(summary = "Khôi phục chi nhánh và tất cả phòng, ghế")
    public ResponseEntity<ApiResponse<BranchResponse>> restoreBranch(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.restoreBranch(id)));
    }

}
