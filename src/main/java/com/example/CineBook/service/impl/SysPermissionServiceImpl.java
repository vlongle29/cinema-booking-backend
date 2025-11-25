package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.SystemFlag;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.service.UserAffectedService;
import com.example.CineBook.common.service.UserCacheEvictEvent;
import com.example.CineBook.dto.sysPermission.SysPermissionRequest;
import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysPermission.SysPermissionSearchDTO;
import com.example.CineBook.mapper.SysPermissionMapper;
import com.example.CineBook.model.SysPermission;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.repository.irepository.SysPermissionRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import com.example.CineBook.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysPermissionRepository sysPermissionRepository;
    private final SysPermissionMapper sysPermissionMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserAffectedService userAffectedService;

    @Override
    @Transactional
    public SysPermissionResponse create(SysPermissionRequest request) {
        sysPermissionRepository.findByPermission(request.getPermission()).ifPresent(p -> {
            throw new BusinessException(MessageCode.SYS_PERMISSION_ALREADY_EXISTS);
        });

        SysPermission entity = sysPermissionMapper.map(request, Collections.emptyMap());
        entity.setSystemFlag(SystemFlag.NORMAL.getValue());

        SysPermission saved = sysPermissionRepository.save(entity);
        return sysPermissionMapper.toResponse(saved, Collections.emptyMap());
    }

    @Override
    @Transactional
    public SysPermissionResponse update(SysPermissionRequest request) {
        SysPermission existing = sysPermissionRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(MessageCode.SYS_PERMISSION_NOT_FOUND));

        if (SystemFlag.SYSTEM.getValue().equals(existing.getSystemFlag())) {
            throw new BusinessException(MessageCode.SYS_PERMISSION_CANNOT_MODIFY_SYSTEM_PERMISSION);
        }

        sysPermissionRepository.findByPermission(request.getPermission()).ifPresent(permissionWithSameCode -> {
            if (!permissionWithSameCode.getId().equals(existing.getId())) {
                throw new BusinessException(MessageCode.SYS_PERMISSION_ALREADY_EXISTS);
            }
        });

        sysPermissionMapper.update(request, existing, Collections.emptyMap());

        SysPermission updated = sysPermissionRepository.save(existing);

        List<UUID> affectedUserIds = userAffectedService.getUserIdsByPermission(updated.getId());
        eventPublisher.publishEvent(new UserCacheEvictEvent(affectedUserIds));

        return sysPermissionMapper.toResponse(updated, Collections.emptyMap());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        SysPermission permissionToDelete = sysPermissionRepository.findById(id).orElseThrow(() -> new BusinessException(MessageCode.SYS_PERMISSION_NOT_FOUND));

        if (SystemFlag.SYSTEM.getValue().equals(permissionToDelete.getSystemFlag())) {
            throw new BusinessException(MessageCode.SYS_PERMISSION_CANNOT_DELETE_SYSTEM_PERMISSION);
        }

        sysPermissionRepository.softDeleteById(id);
        // Xóa cache cho các user bị ảnh hưởng
        List<UUID> affectedUserIds = userAffectedService.getUserIdsByPermission(id);
        eventPublisher.publishEvent(new UserCacheEvictEvent(affectedUserIds));
    }

    @Transactional
    @Override
    public void deleteBatch(List<UUID> ids) {
        List<SysPermission> permissionsToDelete = sysPermissionRepository.findAllById(ids);
        if (permissionsToDelete.size() != ids.size()) {
            throw new BusinessException(MessageCode.SYS_PERMISSION_NOT_FOUND);
        }
        permissionsToDelete.forEach(this::checkNotSystemPermission);
        sysPermissionRepository.softDeleteByIds(permissionsToDelete.stream().map(AuditingEntity::getId).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SysPermissionResponse> findById(UUID id) {
        return sysPermissionRepository.findById(id).map(entity -> sysPermissionMapper.toResponse(entity, Collections.emptyMap()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SysPermissionResponse> search(SysPermissionSearchDTO searchDTO) {
        Page<SysPermission> entityPage = sysPermissionRepository.findAllWithFilters(searchDTO);
        Page<SysPermissionResponse> responsePage = sysPermissionMapper.mapPage(entityPage, Collections.emptyMap());
        return PageResponse.of(responsePage);
    }

    /**
     * Checks if a permission is a system-level permission.
     *
     * @param permission The permission to check.
     */
    private void checkNotSystemPermission(SysPermission permission) {
        if (SystemFlag.SYSTEM.getValue().equals(permission.getSystemFlag())) {
            throw new BusinessException(MessageCode.SYS_PERMISSION_CANNOT_DELETE_SYSTEM_PERMISSION);
        }
    }

    @Override
    @Transactional
    @Cacheable(value = "permissionInfo", key = "#userId")
    public List<String> getPermissionInfoByUserId(UUID userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND);
        }

        List<UUID> roleIds = sysUserRoleRepository.findSysRoleByUserId(userId);
        if (roleIds == null) {
            throw new BusinessException(MessageCode.USER_WITH_NO_ROLE);
        }

        return sysPermissionRepository.getPermissionInfoByUserId(userId);

    }
}
