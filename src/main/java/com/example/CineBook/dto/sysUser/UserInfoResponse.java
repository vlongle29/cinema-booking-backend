package com.example.CineBook.dto.sysUser;

import com.example.CineBook.dto.sysRole.RoleInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserInfoResponse {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String lockFlag;
    private String systemFlag;
    // Trường mới để chứa danh sách vai trò của người dùng
    private List<RoleInfo> roles;
}
