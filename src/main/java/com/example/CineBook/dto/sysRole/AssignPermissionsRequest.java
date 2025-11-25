package com.example.CineBook.dto.sysRole;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AssignPermissionsRequest {
    @NotNull(message = "ID vai trò không được để trống")
    private UUID roleId;

    private List<UUID> permissionIds;
}
