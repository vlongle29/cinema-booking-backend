package com.example.CineBook.service;

import com.example.CineBook.dto.auth.LoginResponse;

public interface GoogleOAuthService {
    LoginResponse authenticateAndFetchProfile(String code, String device, String ipAddress);
}
