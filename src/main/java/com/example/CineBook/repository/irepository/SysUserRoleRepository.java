package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, UUID> {

    Optional<SysUserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    List<SysUserRole> findByUserId(UUID userId);

    void deleteByUserIdAndRoleIdIn(UUID userId, List<UUID> roleIds);

    @Query("SELECT ur.roleId FROM SysUserRole ur WHERE ur.userId = :userId")
    List<UUID> findSysRoleByUserId(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from SysUserRole u where u.userId = :userId")
    void deleteByUserId(UUID userId);

    @Query("select u.userId from SysUserRole u where u.roleId = :roleId")
    List<UUID> findUserIdsByRoleId(@Param("roleId") UUID roleId);

    @Query("select u.userId from SysUserRole u where u.roleId in :roleIds")
    List<UUID> findUserIdsByRoleIds(@Param("roleIds") List<UUID> roleIds);

    boolean existsByUserId(UUID userId);
}
