package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.sysPermission.SysPermissionSearchDTO;
import com.example.CineBook.model.SysPermission;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import com.example.CineBook.repository.custom.SysPermissionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.example.CineBook.model.SysPermission_.permission;

@Repository
public interface SysPermissionRepository extends BaseRepositoryCustom<SysPermission, SysPermissionSearchDTO>, JpaRepository<SysPermission, UUID>, SysPermissionRepositoryCustom {
    Optional<SysPermission> findByPermission(String permission);
}
