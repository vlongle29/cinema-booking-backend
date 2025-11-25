package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.auth.AuthorityProjection;
import com.example.CineBook.dto.sysUser.SysUserSearchDTO;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import com.example.CineBook.repository.custom.SysUserRepositoryCustom;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SysUserRepository extends BaseRepositoryCustom<SysUser, SysUserSearchDTO>, SysUserRepositoryCustom, JpaRepository<SysUser, UUID> {
    Optional<SysUser> findByEmail(@NotBlank(message = "{USER_EMAIL_REQUIRED}") String email);
    Optional<SysUser> findByUsername(@NotBlank(message = "{USER_USERNAME_REQUIRED}") String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByPhone(String phone);
//    // Câu lệnh SQL tối ưu lấy tất cả trong 1 lần
//    @Query(value = """
//        SELECT
//            r.code as roleCode,
//            p.code as permissionCode
//        FROM sys_role r
//        INNER JOIN users_roles ur ON r.id = ur.role_id
//        LEFT JOIN roles_permissions rp ON r.id = rp.role_id
//        LEFT JOIN sys_permission p ON rp.permission_id = p.id
//        WHERE ur.user_id = :userId
//    """, nativeQuery = true)
//    List<AuthorityProjection> findAllAuthoritiesByUserId(@Param("userId") UUID userId);
}
