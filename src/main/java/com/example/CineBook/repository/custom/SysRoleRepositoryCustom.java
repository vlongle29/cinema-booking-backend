package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysRole.SysRoleResponse;
import com.example.CineBook.dto.sysUserRole.UserRoleProjection;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface SysRoleRepositoryCustom {
    List<SysRoleResponse> getRoleInfoByUserId(UUID userId);

    List<UserRoleProjection> findUserRolesByUserIds(List<UUID> userIds);

    List<SysPermissionResponse> findPermissionsByRoleId(UUID roleId);

    List<String> getRoleCodesByUserId(UUID userId);
}

