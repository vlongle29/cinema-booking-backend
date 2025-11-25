package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.SystemFlag;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.service.UserAffectedService;
import com.example.CineBook.common.service.UserCacheEvictEvent;
import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysRole.*;
import com.example.CineBook.mapper.SysRoleMapper;
import com.example.CineBook.model.SysRole;
import com.example.CineBook.model.SysRolePermission;
import com.example.CineBook.model.SysUserRole;
import com.example.CineBook.repository.irepository.SysRolePermissionRepository;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import com.example.CineBook.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleRepository sysRoleRepository;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final UserAffectedService userAffectedService;

    /**
     * Tạo mới một Role từ SysRoleRequest.
     * Kiểm tra sự tồn tại của 'code' trước khi tạo.
     *
     * @param request Dữ liệu đầu vào từ client
     * @return SysRoleResponse sau khi lưu thành công
     */
    @Override
    @Transactional
    public SysRoleResponse create(SysRoleRequest request) {
        // 1. Kiểm tra xem code đã tồn tại chưa (không phân biệt hoa thường)
        sysRoleRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(role -> {
            throw new BusinessException(MessageCode.SYS_ROLE_CODE_ALREADY_EXISTS);
        });

        // 2. Chuyển đổi từ request sang entity
        SysRole entity = sysRoleMapper.map(request, Collections.emptyMap());
        entity.setSystemFlag(SystemFlag.NORMAL.getValue());

        // 3. Lưu và trả về DTO
        SysRole saved = sysRoleRepository.save(entity);
        return sysRoleMapper.toResponse(saved, Collections.emptyMap());
    }

    /**
     * Cập nhật Role theo id.
     *
     * @param request Dữ liệu cập nhật
     * @return SysRoleResponse sau khi cập nhật
     */
    @Override
    @Transactional
    public SysRoleResponse update(SysRoleRequest request) {
        SysRole entity = sysRoleRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(MessageCode.SYS_ROLE_NOT_FOUND));

        sysRoleMapper.update(request, entity);
        SysRole updated = sysRoleRepository.save(entity);

        List<UUID> affectedUserIds = userAffectedService.getUserIdsByRole(updated.getId());
        eventPublisher.publishEvent(new UserCacheEvictEvent(affectedUserIds));

        return sysRoleMapper.toResponse(updated, Collections.emptyMap());
    }

    /**
     * Xóa Role theo id (xóa mềm).
     *
     * @param id UUID của Role cần xóa
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        // 1. Tìm vai trò cần xóa
        SysRole roleToDelete = sysRoleRepository.findById(id).orElseThrow(() -> new BusinessException(MessageCode.SYS_ROLE_NOT_FOUND));

        // 2. Kiểm tra nếu đây là vai trò hệ thống. Vai trò hệ thống không thể bị xóa.
        if (SystemFlag.SYSTEM.equals(roleToDelete.getSystemFlag())) {
            throw new BusinessException(MessageCode.SYS_ROLE_CANNOT_DELETE_SYSTEM_ROLE);
        }

        // 3. Nếu hợp lệ, tiến hành xóa mềm
        sysRoleRepository.softDeleteById(id);
        // Xóa cache cho các user bị ảnh hưởng
        List<UUID> affectedUserIds = userAffectedService.getUserIdsByRole(id);
        eventPublisher.publishEvent(new UserCacheEvictEvent(affectedUserIds));
    }

    @Override
    @Transactional
    public void deleteRoles(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        for (UUID id : roleIds) {
            this.delete(id);
        }
    }

    /**
     * Tìm kiếm Role theo id.
     *
     * @param id UUID của Role
     * @return Optional<SysRoleResponse> nếu tìm thấy
     */
    @Override
    @Transactional(readOnly = true)
    public SysRoleResponse findById(UUID id) {
        return sysRoleRepository.findById(id).map(entity -> sysRoleMapper.toResponse(entity, Collections.emptyMap())).orElseThrow(() -> new BusinessException(MessageCode.SYS_ROLE_NOT_FOUND));
    }

    /**
     * Lấy tất cả các Role chưa bị xóa.
     *
     * @return Danh sách SysRoleResponse
     */
    @Override
    @Transactional(readOnly = true)
    public List<SysRoleResponse> findAll() {
        List<SysRole> entityList = sysRoleRepository.findAllWithFiltersAndSort(new SysRoleSearchDTO());
        return sysRoleMapper.toResponseList(entityList, Collections.emptyMap());
    }

    /**
     * Tìm kiếm Role có phân trang và filter.
     *
     * @param searchDTO Thông tin tìm kiếm và phân trang
     * @return Page<SysRoleResponse> kết quả tìm kiếm
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SysRoleResponse> search(SysRoleSearchDTO searchDTO) {
        Page<SysRole> entityPage = sysRoleRepository.findAllWithFilters(searchDTO);
        Page<SysRoleResponse> responsePage = sysRoleMapper.mapPage(entityPage, Collections.emptyMap());
        return PageResponse.of(responsePage);
    }

    /**
     * Gán danh sách quyền (permissions) cho một vai trò (role).
     * Thao tác này sẽ xóa tất cả các quyền hiện có của vai trò và thay thế bằng danh sách quyền mới.
     *
     * @param request Chứa roleId và danh sách các permissionId.
     * @return Phản hồi thành công hoặc lỗi.
     */
    @Override
    @Transactional
    public void assignPermissionsToRole(AssignPermissionsRequest request) {
        UUID roleId = request.getRoleId();

        // 1. Kiểm tra sự tồn tại và cờ hệ thống của vai trò
        SysRole role = sysRoleRepository.findById(roleId).orElseThrow(() -> new BusinessException(MessageCode.SYS_ROLE_NOT_FOUND));

        if (SystemFlag.SYSTEM.equals(role.getSystemFlag())) {
            throw new BusinessException(MessageCode.SYS_ROLE_CANNOT_MODIFY_SYSTEM_ROLE_PERMISSIONS);
        }

        // 2. Xóa tất cả các quyền hiện tại của vai trò này để đảm bảo tính nhất quán
        rolePermissionRepository.deleteByRoleId(roleId);

        // 3. Gán các quyền mới (nếu có)
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            // Sử dụng Set để loại bỏ các permissionId trùng lặp từ request
            List<SysRolePermission> newPermissions = new HashSet<>(request.getPermissionIds()).stream().map(permissionId -> new SysRolePermission(null, roleId, permissionId)).collect(java.util.stream.Collectors.toList());
            rolePermissionRepository.saveAll(newPermissions);
        }
        // Xóa cache cho các user bị ảnh hưởng
        List<UUID> affectedUserIds = userAffectedService.getUserIdsByRole(roleId);
        eventPublisher.publishEvent(new UserCacheEvictEvent(affectedUserIds));
    }

    /**
     * Lấy danh sách các quyền (permissions) dựa vào roleId.
     *
     * @param roleId UUID của vai trò cần kiểm tra.
     * @return Danh sách các quyền.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SysPermissionResponse> getPermissionsByRoleId(UUID roleId) {
        // 1. Kiểm tra xem vai trò có tồn tại không
        if (!sysRoleRepository.existsById(roleId)) {
            throw new BusinessException(MessageCode.SYS_ROLE_NOT_FOUND);
        }
        // 2. Lấy danh sách quyền từ repository
        return sysRoleRepository.findPermissionsByRoleId(roleId);
    }

    // Giả sử có phương thức này
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#userId")
    public List<SysRoleResponse> getRoleInfoByUserId(UUID userId) {
        return sysRoleRepository.getRoleInfoByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRoleToUsers(UUID roleId, AssignRoleToUserRequest request) {
        List<UUID> userIds = request.getUserIds();
        if (userIds == null || userIds.isEmpty()) return;

        for (UUID userId : userIds) {
            if (!sysUserRoleRepository.findByUserIdAndRoleId(userId, roleId).isPresent()) {
                SysUserRole userRole = new SysUserRole(null, userId, roleId);
                sysUserRoleRepository.save(userRole);
            }
        }
        eventPublisher.publishEvent(new UserCacheEvictEvent(userIds));
    }
}
