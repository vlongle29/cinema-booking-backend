package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.sysPermission.SysPermissionRequest;
import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysPermission.SysPermissionSearchDTO;
import com.example.CineBook.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Permission", description = "Các API để quản lý quyền hạn")
@RestController
@RequestMapping("/api/permissions")
public class SysPermissionController {
    private final SysPermissionService sysPermissionService;

    @Autowired
    public SysPermissionController(SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    @Operation(summary = "Tạo một quyền mới")
    @PostMapping
//    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<ApiResponse<SysPermissionResponse>> createPermission(@Valid @RequestBody SysPermissionRequest request) {
        SysPermissionResponse newPermission = sysPermissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newPermission));
    }

    @Operation(summary = "Cập nhật một quyền đã có")
    @PutMapping
//    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<ApiResponse<SysPermissionResponse>> updatePermission(@Valid @RequestBody SysPermissionRequest request) {
        SysPermissionResponse updatedPermission = sysPermissionService.update(request);
        return ResponseEntity.ok(ApiResponse.success( updatedPermission));
    }

    @Operation(summary = "Xóa một quyền")
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable UUID id) {
        sysPermissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "Xóa nhiều quyền")
    @DeleteMapping("/batch")
//    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<ApiResponse<Void>> deletePermissionsBatch(@RequestBody List<UUID> ids) {
        sysPermissionService.deleteBatch(ids);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "Tìm kiếm và phân trang các quyền")
    @PostMapping("/search")
//    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<PageResponse<SysPermissionResponse>>> searchPermissions(@RequestBody SysPermissionSearchDTO searchDTO) {
        PageResponse<SysPermissionResponse> results = sysPermissionService.search(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "Lấy thông tin chi tiết của một quyền")
    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<SysPermissionResponse>> getPermissionById(@PathVariable UUID id) {
        SysPermissionResponse permission = sysPermissionService.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.SYS_PERMISSION_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(permission));
    }
}
