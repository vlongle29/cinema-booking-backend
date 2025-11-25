package com.example.CineBook.dto.sysPermission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SysPermissionRequest {
    private UUID id;

    @NotEmpty(message = "{PERMISSION_NAME_REQUIRED}")
    @Size(max = 100, message = "{PERMISSION_NAME_MAX_SIZE}")
    private String permission;

    @Size(max = 500, message = "{PERMISSION_DESCRIPTION_MAX_SIZE}")
    private String description;
}
