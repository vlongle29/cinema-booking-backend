package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysRole.*;
import com.example.CineBook.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Role", description = "Các API để quản lý vai trò và quyền hạn")
@RestController
@RequestMapping("/api/roles")
public class SysRoleController {
    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @Operation(summary = "Tạo một vai trò mới")
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<SysRoleResponse>> createRole(@Valid @RequestBody SysRoleRequest request) {
        SysRoleResponse newRole = sysRoleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vai trò đã được tạo thành công.", newRole));
    }

    @Operation(summary = "Cập nhật một vai trò đã có")
    @PutMapping
//    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<ApiResponse<SysRoleResponse>> updateRole(@Valid @RequestBody SysRoleRequest request) {
        SysRoleResponse updatedRole = sysRoleService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Vai trò đã được cập nhật thành công.", updatedRole));
    }

    /*@Operation(summary = "Xóa một vai trò")
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        sysRoleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }*/

    @Operation(summary = "Xóa 1 hoặc nhiều quyền")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteRoles(@RequestBody DeleteRoleRequest request) {
        sysRoleService.deleteRoles(request.getRoleIds());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "Tìm kiếm và phân trang các vai trò")
    @PostMapping("/search")
//    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<PageResponse<SysRoleResponse>>> searchRoles(@RequestBody SysRoleSearchDTO searchDTO) {
        PageResponse<SysRoleResponse> results = sysRoleService.search(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "Lấy thông tin chi tiết của một vai trò")
    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<SysRoleResponse>> getRoleById(@PathVariable UUID id) {
        SysRoleResponse role = sysRoleService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @Operation(summary = "Lấy danh sách tất cả các vai trò")
    @GetMapping
//    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<List<SysRoleResponse>>> getAllRoles() {
        List<SysRoleResponse> roles = sysRoleService.findAll();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @Operation(summary = "Gán quyền cho một vai trò")
    @PostMapping("/assign-permissions")
//    @PreAuthorize("hasAuthority('role:manage')")
    public ResponseEntity<ApiResponse<Void>> assignPermissionsToRole(@Valid @RequestBody AssignPermissionsRequest request) {
        sysRoleService.assignPermissionsToRole(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "Lấy danh sách các quyền của một vai trò")
    @GetMapping("/{roleId}/permissions")
//    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<List<SysPermissionResponse>>> getPermissionsForRole(@PathVariable UUID roleId) {
        List<SysPermissionResponse> permissions = sysRoleService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @PostMapping("/assign-role-to-users")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUsers(
            @RequestBody AssignRoleToUserRequest request) {
        sysRoleService.assignRoleToUsers(request.getRoleId(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
