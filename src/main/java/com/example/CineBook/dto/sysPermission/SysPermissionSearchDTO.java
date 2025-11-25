package com.example.CineBook.dto.sysPermission;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysPermissionSearchDTO extends SearchBaseDto {
    private String permission;
    private UUID roleId;
}
