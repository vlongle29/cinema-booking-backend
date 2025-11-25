package com.example.CineBook.common.service;

import com.example.CineBook.model.SysRolePermission;
import com.example.CineBook.repository.irepository.SysRolePermissionRepository;
import com.example.CineBook.repository.irepository.SysUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service helper để lấy danh sách userId bị ảnh hưởng bởi các thay đổi role, menu, permission.
 * Tách riêng để tránh vòng lặp phụ thuộc.
 */
@Service
public class UserAffectedService {
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRolePermissionRepository sysRolePermissionRepository;

    @Autowired
    public UserAffectedService(SysUserRoleRepository sysUserRoleRepository,
                               SysRolePermissionRepository sysRolePermissionRepository) {
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysRolePermissionRepository = sysRolePermissionRepository;
    }

    /**
     * Lấy tất cả userId thuộc role này
     */
    public List<UUID> getUserIdsByRole(UUID roleId) {
        return sysUserRoleRepository.findUserIdsByRoleId(roleId);
    }

    /**
     * Lấy tất cả userId thuộc các role này
     */
    public List<UUID> getUserIdsByRoleIds(List<UUID> roleIds) {
        return sysUserRoleRepository.findUserIdsByRoleIds(roleIds);
    }

    /**
     * Lấy tất cả roleId có permission này
     */
    public List<UUID> getRoleIdsByPermission(UUID permissionId) {
        return sysRolePermissionRepository.findByPermissionId(permissionId).stream().map(SysRolePermission::getRoleId).toList();
    }

    /**
     * Lấy tất cả userId bị ảnh hưởng bởi permission
     */
    public List<UUID> getUserIdsByPermission(UUID permissionId) {
        List<UUID> roleIds = getRoleIdsByPermission(permissionId);
        return getUserIdsByRoleIds(roleIds);
    }

}
