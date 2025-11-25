package com.example.CineBook.dto.sysRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysRoleResponse {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Instant createTime;
    private Instant updateTime;
    private UUID updateBy;
}
