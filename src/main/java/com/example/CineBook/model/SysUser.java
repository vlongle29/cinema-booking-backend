package com.example.CineBook.model;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@StaticMetamodel(SysUser.class)
@Builder
public class SysUser extends AuditingEntity implements UserDetails {
    @Column(length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(length = 100)
    private String avatar;

    @Column(length = 10)
    private String lockFlag = "0";

    @Column(length = 100)
    private String providerId; // Google ID, Facebook ID, etc.

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "system_flag", length = 20, nullable = false)
    private String systemFlag; // To distinguish between system users and ordinary users

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO: Triển khai logic để trả về vai trò/quyền hạn của người dùng.
        // Ví dụ: return List.of(new SimpleGrantedAuthority(role.getName()));
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        // Trả về true nếu tài khoản không bao giờ hết hạn.
        // Bạn có thể thêm một trường vào SysUser để quản lý việc này nếu cần.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Tài khoản được coi là "không bị khóa" nếu cờ lockFlag không phải là giá trị của LockFlag.LOCK.
        // Cách triển khai này an toàn: nếu lockFlag là null hoặc một giá trị không mong muốn,
        // tài khoản vẫn được coi là không bị khóa.
        return !LockFlag.LOCK.getValue().equals(this.lockFlag);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Trả về true nếu mật khẩu không bao giờ hết hạn.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Một người dùng được coi là "enabled" nếu họ chưa bị xóa mềm (soft-deleted).
        // Điều này được xác định bằng cách kiểm tra cờ isDelete trong AuditingEntity.
        // Trả về true nếu isDelete là false, ngược lại là false.
        return Boolean.FALSE.equals(this.getIsDelete());
    }

}
