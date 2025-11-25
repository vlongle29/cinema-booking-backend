package com.example.CineBook.dto.sysRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleToUserRequest {
    @NotNull
    private UUID roleId;

    @NotNull
    private List<UUID> userIds;
}
