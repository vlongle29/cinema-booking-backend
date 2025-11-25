package com.example.CineBook.dto.sysRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRoleRequest {
    private List<UUID> roleIds;
}
