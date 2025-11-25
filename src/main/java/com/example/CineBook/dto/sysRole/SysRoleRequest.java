package com.example.CineBook.dto.sysRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SysRoleRequest {
    private UUID id;

    @NotBlank(message = "{ROLE_NAME_REQUIRED}")
    @Size(max = 500, message = "{ROLE_NAME_MAX_SIZE}")
    private String name;

    @NotBlank(message = "{ROLE_CODE_REQUIRED}")
    @Size(max = 100, message = "{ROLE_CODE_MAX_SIZE}")
    private String code;

    @Size(max = 500, message = "{ROLE_DESCRIPTION_MAX_SIZE}")
    private String description;
}
