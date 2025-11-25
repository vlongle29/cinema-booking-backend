package com.example.CineBook.repository.custom;

import java.util.List;
import java.util.UUID;

public interface SysPermissionRepositoryCustom {
    List<String> getPermissionInfoByUserId(UUID userId);
}
