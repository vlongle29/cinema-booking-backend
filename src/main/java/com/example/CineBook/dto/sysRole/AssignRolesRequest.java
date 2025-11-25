package com.example.CineBook.dto.sysRole;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AssignRolesRequest {
    @NotNull(message = "{ASSIGN_ROLE_USER_ID_REQUIRED}")
    private UUID userId;

    @NotNull(message = "{ASSIGN_ROLE_ROLE_IDS_REQUIRED}")
    private List<UUID> roleIds;
}
