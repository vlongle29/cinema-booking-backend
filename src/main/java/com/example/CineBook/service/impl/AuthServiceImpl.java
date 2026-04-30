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
import com.example.CineBook.model.Customer;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUserRole;
import com.example.CineBook.repository.irepository.CustomerRepository;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import com.example.CineBook.repository.redis.BlackListedTokenRepository;
import com.example.CineBook.service.AuthService;
import com.example.CineBook.service.SysPermissionService;
import com.example.CineBook.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final SysUserRepository userRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final CustomerRepository customerRepository;
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
            throw new BusinessException(MessageCode.PASSWORD_RESET_FAILED);
        }

        // Check existing user
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(MessageCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(MessageCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(MessageCode.PHONE_ALREADY_EXISTS);
        }

        // Create new user
        SysUser user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Assign default CUSTOMER role and set typeAccount
        UUID roleId = assignDefaultRole(user.getId());
        SysRole role = sysRoleRepository.findById(roleId).orElse(null);
        if (role != null) {
            user.setTypeAccount(role.getCode());
            userRepository.save(user);
        }

        // Create customer profile
        Customer customer = Customer.builder()
                .userId(user.getId())
                .build();
        customerRepository.save(customer);

        return authMapper.map(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String device, String ipAddress) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(MessageCode.ACCOUNT_NOT_EXISTS));

        // if the account is locked
        if (user.getLockFlag().equals(LockFlag.LOCK.getValue())) {
            throw new BusinessException(MessageCode.ACCOUNT_LOCKED);
        }

        // if the password is wrong
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockFlag(LockFlag.LOCK.getValue());
            }
            userRepository.save(user);
            throw new BusinessException(MessageCode.PASSWORD_WRONG);
        }

        // if login wrong > 0 then update failedLoginAttempts = 0
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
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
        Date createdAt = new Date();

        // Hash refreshToken before storing
        String hashedRefreshToken = jwtTokenProvider.hashToken(refreshToken);

        // Save session with hashed refresh token
        SessionInfo sessionInfo = new SessionInfo(
            user.getId(), 
            user.getUsername(), 
            hashedRefreshToken,
            expiry, 
            device, 
            ipAddress,
            createdAt
        );
        redisTemplate.opsForValue().set(sessionId.toString(), sessionInfo, 8, TimeUnit.DAYS);

        UserInfoResponse userInfo = sysUserService.getUserDetail(user.getId());
        List<String> permissions = sysPermissionService.getPermissionInfoByUserId(user.getId());

        return new LoginResponse(accessToken, userInfo, permissions, sessionId, refreshToken);
    }

    /**
     * PRODUCTION-GRADE REFRESH TOKEN IMPLEMENTATION
     * 
     * Security measures:
     * 1. Validates JWT signature of refreshToken
     * 2. Verifies hashed refreshToken matches Redis
     * 3. Checks session expiry
     * 4. Implements refresh token rotation (prevents replay attacks)
     * 5. Invalidates old refresh token immediately
     * 6. Multi-device support via sessionId
     * 
     * Flow:
     * - Client sends: sessionId cookie + refreshToken cookie
     * - Server validates both credentials
     * - Generates new accessToken + new refreshToken
     * - Rotates refreshToken in Redis
     * - Returns new tokens via cookies
     */
    @Override
    public LoginResponse refreshToken(UUID sessionId, String refreshToken) {
        try {
            // Step 1: Validate JWT signature and structure
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                redisTemplate.delete(sessionId.toString());
                throw new BusinessException(MessageCode.INVALID_TOKEN);
            }

            // Step 2: Retrieve session from Redis
            SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionId.toString());
            if (sessionInfo == null) {
                throw new BusinessException(MessageCode.SESSION_NOT_FOUND);
            }

            // Step 3: Verify session expiry
            if (sessionInfo.expiry().before(new Date())) {
                redisTemplate.delete(sessionId.toString());
                throw new BusinessException(MessageCode.SESSION_EXPIRED);
            }

            // Step 4: Hash incoming refreshToken and compare with stored hash
            String hashedRefreshToken = jwtTokenProvider.hashToken(refreshToken);
            if (!hashedRefreshToken.equals(sessionInfo.hashedRefreshToken())) {
                // Potential replay attack - invalidate session immediately
                redisTemplate.delete(sessionId.toString());
                throw new BusinessException(MessageCode.INVALID_TOKEN);
            }

            // Step 5: Generate new tokens (rotation)
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    sessionInfo.userId().toString(), null, null
            );

            String newAccessToken = jwtTokenProvider.generateToken(authentication, sessionId, sessionInfo.username());
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(sessionInfo.userId().toString(), sessionId);

            // Step 6: Update session with new hashed refresh token
            Date newExpiry = new Date(System.currentTimeMillis() + 8L * 24 * 60 * 60 * 1000);
            String newHashedRefreshToken = jwtTokenProvider.hashToken(newRefreshToken);
            
            SessionInfo updatedSessionInfo = new SessionInfo(
                sessionInfo.userId(), 
                sessionInfo.username(), 
                newHashedRefreshToken,
                newExpiry, 
                sessionInfo.device(), 
                sessionInfo.ipAddress(),
                sessionInfo.createdAt()
            );
            
            // Step 7: Atomically update Redis (old refresh token is now invalid)
            redisTemplate.opsForValue().set(sessionId.toString(), updatedSessionInfo, 8, TimeUnit.DAYS);

            // Step 8: Prepare response
            UserInfoResponse userInfo = sysUserService.getUserDetail(sessionInfo.userId());
            List<String> permissions = sysPermissionService.getPermissionInfoByUserId(sessionInfo.userId());

            return new LoginResponse(newAccessToken, userInfo, permissions, sessionId, newRefreshToken);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("REFRESH TOKEN ERROR", e);
            throw new BusinessException(MessageCode.LOGIN_FAIL);
        }
    }

    @Override
    public void logout(String authHeader, UUID sessionId) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(MessageCode.INVALID_TOKEN);
        }

        if (sessionId == null) {
            throw new BusinessException(MessageCode.SESSION_NOT_FOUND);
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

        // Delete session from Redis (invalidates refresh token)
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