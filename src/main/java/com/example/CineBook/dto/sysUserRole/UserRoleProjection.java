package com.example.CineBook.dto.sysUserRole;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserRoleProjection {
    private UUID userId;
    private UUID roleId;
    private String roleName;
    private String roleCode;
}
