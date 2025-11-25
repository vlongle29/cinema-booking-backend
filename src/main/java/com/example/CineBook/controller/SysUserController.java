package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.auth.ChangePasswordRequest;
import com.example.CineBook.dto.auth.ResetPasswordRequest;
import com.example.CineBook.dto.auth.UserResetPasswordRequest;
import com.example.CineBook.dto.sysUser.*;
import com.example.CineBook.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User", description = "Quản lý người dùng: tạo, cập nhật, xoá, tìm kiếm user")
@RestController
@RequestMapping("/api/users")
public class SysUserController {
    private final SysUserService sysUserService;

    @Autowired
    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "Tạo người dùng mới", description = "Tạo mới một user với thông tin được cung cấp.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserInfoResponse>> createUser(UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sysUserService.createUser(request)));
    }

    @Operation(summary = "Cập nhật thông tin người dùng", description = "Cập nhật thông tin user dựa trên ID trong đường dẫn.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sysUserService.updateUser(userId, request)));
    }

    @Operation(summary = "Xoá người dùng", description = "Xoá một user dựa trên ID trong đường dẫn.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(@RequestBody UserIdRequest request) {
        sysUserService.deleteUser(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sysUserService.getUserDetail(id)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserInfoResponse>>> searchUser(@RequestBody SysUserSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(sysUserService.searchUsers(searchDTO)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody ChangePasswordRequest request) {
        sysUserService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<Integer>> lockUsers(@RequestBody UserIdRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sysUserService.lockUsers(request)));
    }

    @PostMapping("/unlock")
    public ResponseEntity<ApiResponse<Integer>> unlockUsers(@RequestBody UserIdRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sysUserService.unlockUsers(request)));
    }

    @PostMapping("/reset-password-user")
    public ResponseEntity<ApiResponse<Void>> resetPasswordByEmail(@RequestBody UserResetPasswordRequest request) {
        sysUserService.sendResetPasswordByEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
