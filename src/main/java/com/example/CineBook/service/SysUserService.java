package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.auth.ChangePasswordRequest;
import com.example.CineBook.dto.auth.ResetPasswordRequest;
import com.example.CineBook.dto.auth.UserResetPasswordRequest;
import com.example.CineBook.dto.sysRole.AssignRolesRequest;
import com.example.CineBook.dto.sysUser.*;

import java.util.UUID;

public interface SysUserService {
    UserInfoResponse createUser(UserCreateRequest request);
    UserInfoResponse updateUser(UUID userId, UserUpdateRequest request);
    void deleteUser(UserIdRequest request);
    UserInfoResponse getUserDetail(UUID id);
    PageResponse<UserInfoResponse> searchUsers(SysUserSearchDTO searchDTO);
    void changePassword(ChangePasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    int lockUsers(UserIdRequest request);
    int unlockUsers(UserIdRequest request);
    void assignRolesToUser(AssignRolesRequest request);
    void sendResetPasswordByEmail(UserResetPasswordRequest request);
}
