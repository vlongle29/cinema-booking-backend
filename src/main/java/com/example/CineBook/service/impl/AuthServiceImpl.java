package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.common.constant.RoleEnum;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.security.JwtTokenProvider;
import com.example.CineBook.common.security.SecurityUtils;
import com.example.CineBook.common.security.SessionInfo;
import com.example.CineBook.dto.auth.LoginRequest;
import com.example.CineBook.dto.auth.LoginResponse;
import com.example.CineBook.dto.auth.RegisterRequest;
import com.example.CineBook.dto.auth.RegisterResponse;
import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.example.CineBook.mapper.AuthMapper;
import com.example.CineBook.mapper.UserMapper;
import com.example.CineBook.model.BlacklistedToken;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUserRole;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import com.example.CineBook.repository.redis.BlackListedTokenRepository;
import com.example.CineBook.service.AuthService;
import com.example.CineBook.service.SysPermissionService;
import com.example.CineBook.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SysUserRepository userRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserService sysUserService;
    private final SysPermissionService sysPermissionService;
    private final BlackListedTokenRepository blackListedTokenRepository;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(MessageCode.PASSWORD_RESET_FAILED.name());
        }

        // Check existing user
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(MessageCode.USERNAME_ALREADY_EXISTS.name());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(MessageCode.EMAIL_ALREADY_EXISTS.name());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException(MessageCode.PHONE_ALREADY_EXISTS.name());
        }

        // Create new user
        SysUser user = userMapper.toEntity(request);
        userRepository.save(user);

        // Assign default CUSTOMER role and set typeAccount
        UUID roleId = assignDefaultRole(user.getId());
        SysRole role = sysRoleRepository.findById(roleId).orElse(null);
        if (role != null) {
            user.setTypeAccount(role.getCode());
            userRepository.save(user);
        }

        return authMapper.map(request);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, String device, String ipAddress) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(MessageCode.ACCOUNT_NOT_EXISTS.name()));

        // if the account is locked
        if (user.getLockFlag().equals(LockFlag.LOCK.getValue())) {
            throw new IllegalArgumentException(MessageCode.ACCOUNT_LOCKED.name());
        }

        // if the password is wrong
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockFlag(LockFlag.LOCK.getValue());
            }
            userRepository.save(user);
            throw new IllegalArgumentException(MessageCode.PASSWORD_WRONG.name());
        }

        // if login wrong > 0 then update failedLoginAttempts = 0
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        // if the account is locked
        if (user.getLockFlag().equals(LockFlag.LOCK.getValue())) {
            throw new IllegalArgumentException("Tài khoản đã bị khóa");
        }

        return buildLoginResponse(user, device, ipAddress);
    }

    public LoginResponse buildLoginResponse(SysUser user, String device, String ipAddress) {
        UUID sessionId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId().toString(), null, null);
        String accessToken = jwtTokenProvider.generateToken(authentication, sessionId, user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString(), sessionId);

        // Create expiry date (8 days)
        Date expiry = new Date(System.currentTimeMillis() + 8L * 24 * 60 * 60 * 1000);

        // Hash refreshToken
        String hashedRefreshToken = jwtTokenProvider.hashToken(refreshToken);

        // Save refreshToken into session
        SessionInfo sessionInfo = new SessionInfo(user.getId(), user.getUsername(), hashedRefreshToken, expiry, device, ipAddress);
        redisTemplate.opsForValue().set(sessionId.toString(), sessionInfo, 8, TimeUnit.DAYS);

        UserInfoResponse userInfo = sysUserService.getUserDetail(user.getId());
        List<String> permissions = sysPermissionService.getPermissionInfoByUserId(user.getId());

        return new LoginResponse(accessToken, userInfo, permissions, sessionId);
    }

    /**
     * Refresh access token bằng refresh token còn hạn.
     * - Kiểm tra sessionId trong Redis
     * - Kiểm tra hạn của refreshToken
     * - Tạo accessToken + refreshToken mới
     * - Lưu session mới vào Redis với TTL = 8 ngày
     * - Trả về LoginResponse chứa token mới và thông tin user
     */
    @Override
    public LoginResponse refreshToken(UUID sessionId) {
        UUID newSessionId = UUID.randomUUID();

        try {
            // Check sessionId exists in redis
            SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionId.toString());
            if (sessionInfo == null) {
                throw new BusinessException(MessageCode.SESSION_NOT_FOUND);
            }

            // Check if refreshToken still valid
            if (sessionInfo.expiry().before(new Date())) {
                redisTemplate.delete(sessionId.toString());
                throw new BusinessException(MessageCode.SESSION_EXPIRED);
            }

            // Create new accessToken and refreshToken
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    sessionInfo.userId().toString(), null, null
            );
            String username = sessionInfo.username();
            String newAccessToken = jwtTokenProvider.generateToken(authentication, newSessionId, username);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(sessionInfo.userId().toString(), newSessionId);

            // Create expiry date (8 days)
            Date expiry = new Date(System.currentTimeMillis() + 8L * 24 * 60 * 60 * 1000);
            // Create new SessionInfo object
            sessionInfo = new SessionInfo(sessionInfo.userId(), sessionInfo.username(), newRefreshToken, expiry, sessionInfo.device(), sessionInfo.ipAddress());
            // Save new session in Redis
            redisTemplate.opsForValue().set(newSessionId.toString(), sessionInfo, 8, TimeUnit.DAYS);

            // Delete old sessionId
            redisTemplate.delete(sessionId.toString());

            UserInfoResponse userInfo = sysUserService.getUserDetail(sessionInfo.userId());
            List<String> permissions = sysPermissionService.getPermissionInfoByUserId(sessionInfo.userId());

            return new LoginResponse(newAccessToken, userInfo, permissions, newSessionId);

        } catch (Exception e) {
            throw new BusinessException(MessageCode.LOGIN_FAIL);
        }
    }

    @Override
    public void logout(String authHeader, UUID sessionId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(MessageCode.INVALID_TOKEN);
        }

        String accessToken = authHeader.substring(7);

        // Get expiration time from accessToken
        Date expiresAt = jwtTokenProvider.getExpirationFromToken(accessToken);

        // Calculate TTL (time to live) in seconds
        long ttl = (expiresAt.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0) {
            // Add accessToken to blacklist
            BlacklistedToken blacklistedToken = new BlacklistedToken(
                    accessToken,
                    expiresAt.toInstant(),
                    sessionId,
                    ttl
            );
            blackListedTokenRepository.save(blacklistedToken);
        }

        // Delete session from Redis
        redisTemplate.delete(sessionId.toString());
    }

    /**
     * Assign default role CUSTOMER to user when register successfully
     * @param userId
     */
    private UUID assignDefaultRole(UUID userId) {
        // Check if user already has role
        if (sysUserRoleRepository.existsByUserId(userId)) {
            return sysUserRoleRepository.findByUserId(userId).get(0).getRoleId();
        }
        
        SysRole customerRole = sysRoleRepository.findByCode(RoleEnum.CUSTOMER.getValue())
                .orElseThrow(() -> new IllegalArgumentException("CUSTOMER role not found"));
        
        SysUserRole userRole = SysUserRole.builder()
                .userId(userId)
                .roleId(customerRole.getId())
                .build();
        
        sysUserRoleRepository.save(userRole);
        return customerRole.getId();
    }

    public UserInfoResponse getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().map(UUID::fromString).map(sysUserService::getUserDetail).orElseThrow(() -> new BusinessException(MessageCode.LOGIN_FAIL));
    }

}


/**
 *
 * jwt
 * user detail
 *
 */