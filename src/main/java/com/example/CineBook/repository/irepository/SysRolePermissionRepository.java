package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {
    void deleteByRoleId(UUID roleId);

    Optional<SysRolePermission> findByRoleIdAndPermissionId(UUID id, UUID id1);

    List<SysRolePermission> findByPermissionId(UUID permissionId);
}
