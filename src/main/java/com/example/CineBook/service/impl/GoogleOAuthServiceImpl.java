package com.example.CineBook.service.impl;

import com.example.CineBook.common.config.GoogleOauth2Config;
import com.example.CineBook.common.constant.AuthProvider;
import com.example.CineBook.common.security.JwtTokenProvider;
import com.example.CineBook.dto.auth.AuthResponseDto;
import com.example.CineBook.dto.auth.GoogleProfile;
import com.example.CineBook.dto.auth.LoginResponse;
import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.service.GoogleOAuthService;
import com.example.CineBook.service.SysPermissionService;
import com.example.CineBook.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final SysUserRepository sysUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleOauth2Config googleOauth2Config;
    private final SysUserService sysUserService;
    private final SysPermissionService sysPermissionService;
    private final AuthServiceImpl authService;

    @Override
    @Transactional
    public LoginResponse authenticateAndFetchProfile(String code, String device, String ipAddress) {
        try {
            // 1. Mang 'code' đi đổi lấy Google Access Token
            String googleAccessToken = googleOauth2Config.getGoogleAccessToken(code);

            // 2. Dùng Access Token để lấy thông tin Profile
            GoogleProfile profile = googleOauth2Config.getGoogleProfile(googleAccessToken);

            // 3. Xử lý logic Database
            SysUser user = sysUserRepository.findByEmail(profile.getEmail())
                    .orElseGet(() -> createNewGoogleUser(profile));

            // 4. Tạo Authentication object và sessionId
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getId().toString(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            UUID sessionId = UUID.randomUUID();

            // 5. Sinh JWT nội bộ của hệ thống
            String accessToken = jwtTokenProvider.generateToken(authentication, sessionId, user.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString(), sessionId);

            UserInfoResponse userInfo = sysUserService.getUserDetail(user.getId());
            List<String> permissions = sysPermissionService.getPermissionInfoByUserId(user.getId());

            return authService.buildLoginResponse(user, device, ipAddress);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to authenticate with Google", e);
        }
    }

    private SysUser createNewGoogleUser(GoogleProfile profile) {
        SysUser newUser = new SysUser();
        newUser.setEmail(profile.getEmail());
        newUser.setUsername(profile.getName());
        newUser.setName(profile.getName());
        newUser.setAuthProvider(AuthProvider.GOOGLE);
        return sysUserRepository.save(newUser);
    }
}
