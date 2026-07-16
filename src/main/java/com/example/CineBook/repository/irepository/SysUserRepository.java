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

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SysUserRepository extends BaseRepositoryCustom<SysUser, SysUserSearchDTO>, SysUserRepositoryCustom, JpaRepository<SysUser, UUID> {
    Optional<SysUser> findByEmail(@NotBlank(message = "{USER_EMAIL_REQUIRED}") String email);
    Optional<SysUser> findByUsername(@NotBlank(message = "{USER_USERNAME_REQUIRED}") String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByPhone(String phone);

    @Query("SELECT u.username FROM SysUser u WHERE u.id = :id")
    Optional<String> findUsernameById(@Param("id") UUID id);
    @Query(value = """
        SELECT
            r.code as roleCode,
            p.code as permissionCode
        FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
        LEFT JOIN sys_permission p ON rp.permission_id = p.id
        WHERE ur.user_id = :userId
    """, nativeQuery = true)
    List<AuthorityProjection> findAllAuthoritiesByUserId(@Param("userId") UUID userId);

    @Query("SELECT u.id as id, u.username as username FROM SysUser u WHERE u.id IN :ids")
    List<UserUsernameProjection> findUsernamesByIds(@Param("ids") Set<UUID> ids);

     interface UserUsernameProjection {
        UUID getId();
        String getUsername();
    }
}
