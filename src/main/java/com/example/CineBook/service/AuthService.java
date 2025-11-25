package com.example.CineBook.service;

import com.example.CineBook.dto.auth.LoginRequest;
import com.example.CineBook.dto.auth.LoginResponse;
import com.example.CineBook.dto.auth.RegisterRequest;
import com.example.CineBook.dto.auth.RegisterResponse;
import com.example.CineBook.dto.sysUser.UserInfoResponse;

import java.util.UUID;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request, String device, String ipAddress);
    LoginResponse refreshToken(UUID sessionId);
    void logout(String accessToken, UUID sessionId);
    UserInfoResponse getCurrentUser();
}
