package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.common.constant.SystemFlag;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.service.UserCacheEvictEvent;
import com.example.CineBook.dto.auth.ChangePasswordRequest;
import com.example.CineBook.dto.auth.ResetPasswordRequest;
import com.example.CineBook.dto.auth.UserResetPasswordRequest;
import com.example.CineBook.dto.sysRole.AssignRolesRequest;
import com.example.CineBook.dto.sysUser.*;
import com.example.CineBook.dto.sysRole.RoleInfo;
import com.example.CineBook.dto.sysUserRole.UserRoleProjection;
import com.example.CineBook.mapper.UserMapper;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.SysUserRole;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import com.example.CineBook.service.EmailService;
import com.example.CineBook.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    private final SysUserRepository sysUserRepository;
    private final UserMapper userMapper;
    private final SysRoleRepository sysRolesRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    @Override
    public UserInfoResponse createUser(UserCreateRequest request) {
        if (sysUserRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(MessageCode.USERNAME_ALREADY_EXISTS);
        }
        if (sysUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(MessageCode.EMAIL_ALREADY_EXISTS);
        }
        if (sysUserRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(MessageCode.PHONE_ALREADY_EXISTS);
        }

        SysUser user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSystemFlag(SystemFlag.NORMAL.getValue());
        SysUser savedUser = sysUserRepository.save(user);

        List<UUID> roleIds = request.getRoleIds();
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleRepository.findAllById(roleIds);
            List<SysUserRole> userRoles = roles.stream()
                    .map(role -> {
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(savedUser.getId());
                        userRole.setRoleId(role.getId());
                        return userRole;
                    }).toList();
            sysUserRoleRepository.saveAll(userRoles);
//            throw new BusinessException(MessageCode.USER_WITH_NO_ROLE);
        }

        List<UserRoleProjection> userRoles = sysRoleRepository.findUserRolesByUserIds(List.of(savedUser.getId()));
        List<RoleInfo> roles = userRoles.stream().map(r -> new RoleInfo(r.getRoleId(), r.getRoleName(), r.getRoleCode())).toList();

        UserInfoResponse response = userMapper.toResponse(savedUser);
        response.setRoles(roles);

        return response;
    }

    @Override
    public UserInfoResponse updateUser(UUID userId, UserUpdateRequest request) {
        SysUser user = sysUserRepository.findById(userId).orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));
        userMapper.update(request, user);

        List<UUID> roleIds = request.getRoleIds();
        if (roleIds != null) {
            List<SysRole> roles = sysRoleRepository.findAllById(roleIds);
             if (roles.size() != roleIds.size()) {
                throw new BusinessException(MessageCode.SYS_ROLE_NOT_FOUND);
            }

            List<SysUserRole> currentUserRoles = sysUserRoleRepository.findByUserId(user.getId());
            List<UUID> currentRoleIds = currentUserRoles.stream().map(SysUserRole::getRoleId).toList();

            List<UUID> toAdd = roleIds.stream().filter(id -> !currentRoleIds.contains(id)).toList();
            List<UUID> toRemove = currentRoleIds.stream().filter(id -> !roleIds.contains(id)).collect(Collectors.toList());

            if (!toRemove.isEmpty()) {
                sysUserRoleRepository.deleteByUserIdAndRoleIdIn(user.getId(), toRemove);
            }

            if (!toAdd.isEmpty()) {
                List<SysUserRole> newUserRoles = toAdd.stream().map(roleId -> {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(user.getId());
                    ur.setRoleId(roleId);
                    return ur;
                }).collect(Collectors.toList());
                sysUserRoleRepository.saveAll(newUserRoles);
            }

            // Cập nhật typeAccount theo code của role đầu tiên
            if (!roleIds.isEmpty()) {
                String firstRoleCode = roles.stream().filter(r -> r.getId().equals(roleIds.get(0))).findFirst().map(SysRole::getCode).orElse(null);
                user.setTypeAccount(firstRoleCode);
            } else {
                user.setTypeAccount(null);
            }
        } else {
            user.setTypeAccount(null);
        }

        SysUser updated = sysUserRepository.save(user);
        eventPublisher.publishEvent(new UserCacheEvictEvent(List.of(updated.getId())));
        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(UserIdRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;

        if (request.getIds() != null && !request.getIds().isEmpty()) {
            List<SysUser> users = sysUserRepository.findAllById(request.getIds());

            boolean isDeletingCurrentUser = users.stream().anyMatch(u -> u.getUsername().equals(currentUsername));
            if (isDeletingCurrentUser) {
                throw new BusinessException(MessageCode.DELETE_FAIL);
            }

            boolean hasSystemUser = users.stream().anyMatch(u -> SystemFlag.SYSTEM.getValue().equals(u.getSystemFlag()));
            if (hasSystemUser) {
                throw new BusinessException(MessageCode.DELETE_FAIL);
            }
            request.getIds().forEach(id -> sysUserRoleRepository.deleteByUserId(id));
        }
        int deletedCount = sysUserRepository.softDeleteByIds(request.getIds());
        if (deletedCount == 0 && request.getIds() != null && !request.getIds().isEmpty()) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND);
        }
    }

    @Override
    public PageResponse<UserInfoResponse> searchUsers(SysUserSearchDTO searchDTO) {
        Page<SysUser> userPage = sysUserRepository.findAllWithFilters(searchDTO);
        List<UUID> userIdsOnPage = userPage.getContent().stream().map(SysUser::getId).collect(Collectors.toList());
        List<UserRoleProjection> userRoles = sysRoleRepository.findUserRolesByUserIds(userIdsOnPage);
        Map<UUID, List<RoleInfo>> rolesByUserIdMap = new HashMap<>();
        for (UserRoleProjection proj : userRoles) {
            UUID userId = proj.getUserId();
            RoleInfo roleInfo = new RoleInfo(
                    proj.getRoleId(),
                    proj.getRoleName(),
                    proj.getRoleCode()
            );

            rolesByUserIdMap
                    .computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(roleInfo);
//          Equivalent:
//            List<RoleInfo> roles = rolesByUserIdMap.get(userId);
//            if (roles == null) {
//                roles = new ArrayList<>();
//                rolesByUserIdMap.put(userId, roles);
//            }
//            roles.add(roleInfo);
        }
        Page<UserInfoResponse> userInfoResponsePage = userMapper.mapPageWithRoles(userPage, rolesByUserIdMap);
        return PageResponse.of(userInfoResponsePage);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        SysUser user = sysUserRepository.findById(request.getUserId()).orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(MessageCode.PASSWORD_CHANGE_FAILED);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserRepository.save(user);
        // Phát event xóa cache user info
        eventPublisher.publishEvent(new UserCacheEvictEvent(List.of(user.getId())));
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        SysUser user = sysUserRepository.findById(request.getUserId()).orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserRepository.save(user);
        // Phát event xóa cache user info
        eventPublisher.publishEvent(new UserCacheEvictEvent(List.of(user.getId())));
    }

    @Override
    public int unlockUsers(UserIdRequest request) {
        int unlockedCount = sysUserRepository.updateLockStatusForIds(request.getIds(), LockFlag.NORMAL);
        if (unlockedCount == 0 && request.getIds() != null && !request.getIds().isEmpty()) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND);
        }
        // Phát event xóa cache user info
        if (request.getIds() != null) {
            eventPublisher.publishEvent(new UserCacheEvictEvent(request.getIds()));
        }
        return unlockedCount;
    }

    @Cacheable(value = "userInfo", key = "#userId")
    @Override
    public UserInfoResponse getUserDetail(UUID userId) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        UserInfoResponse userInfoResponse = userMapper.toUserInfoResponse(user);
        log.info("Building login response for user info: {}", userInfoResponse);
        List<UserRoleProjection> userRoles = sysRolesRepository.findUserRolesByUserIds(List.of(userId));
        log.info("Building login response for user role: {}", userRoles);
        List<RoleInfo> roles = userRoles.stream().map(r -> new RoleInfo(r.getRoleId(), r.getRoleName(), r.getRoleCode())).toList();
        userInfoResponse.setRoles(roles);

        return userInfoResponse;
    }

    @Override
    public int lockUsers(UserIdRequest request) {
        int lockedCount = sysUserRepository.updateLockStatusForIds(request.getIds(), LockFlag.LOCK);
        if (lockedCount == 0 && request.getIds() != null && !request.getIds().isEmpty()) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND);
        }
        // Phát event xóa cache user info
        if (request.getIds() != null) {
            eventPublisher.publishEvent(new UserCacheEvictEvent(request.getIds()));
        }
        return lockedCount;
    }

    /**
     * Gán vai trò cho người dùng.
     * <p>
     * Thao tác này sẽ thay thế hoàn toàn các vai trò hiện tại của người dùng bằng danh sách vai trò mới được cung cấp.
     * 1. Kiểm tra xem người dùng có tồn tại không.
     * 2. Xóa tất cả các vai trò cũ của người dùng trong một câu lệnh DELETE.
     * 3. Thêm tất cả các vai trò mới trong một thao tác INSERT hàng loạt (batch insert).
     *
     * @param request Chứa ID người dùng và danh sách ID vai trò mới.
     */
    @Transactional
    @Override
    @CacheEvict(value = "userInfo", key = "#request.userId")
    public void assignRolesToUser(AssignRolesRequest request) {
        UUID userId = request.getUserId();
        // Đảm bảo danh sách roleIds không null và không có phần tử trùng lặp
        List<UUID> roleIds = request.getRoleIds() == null ? Collections.emptyList() : request.getRoleIds().stream().distinct().collect(Collectors.toList());

        // 1. Check if user exists
        if (!sysUserRepository.existsById(userId)) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND);
        }

        // 2. Xóa tất cả các role hiện tại của user để đảm bảo không bị trùng và loại bỏ các role cũ.
        sysUserRoleRepository.deleteByUserId(userId);

        if (!roleIds.isEmpty()) {
            List<SysUserRole> newUserRoles = roleIds.stream().map(roleId -> new SysUserRole(null, userId, roleId)).collect(Collectors.toList());
            // 3. Lưu tất cả các role mới trong một batch operation để tối ưu hiệu suất.
            sysUserRoleRepository.saveAll(newUserRoles);
        }
        // Phát event xóa cache user info
        eventPublisher.publishEvent(new UserCacheEvictEvent(List.of(userId)));
    }

    @Override
    public void sendResetPasswordByEmail(UserResetPasswordRequest request) {
        SysUser user = sysUserRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        String newResetPassword = generateRandomPassword();

        emailService.sendResetPasswordByEmail(user.getEmail(), user.getUsername(), newResetPassword);

        user.setPassword(passwordEncoder.encode(newResetPassword));
        sysUserRepository.save(user);

        eventPublisher.publishEvent(new UserCacheEvictEvent(List.of(user.getId())));
    }


    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

}
