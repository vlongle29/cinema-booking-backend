package com.example.CineBook.service;

import java.util.Optional;
import java.util.UUID;

public interface SysUserRoleService {
    Optional<UUID> findRoleByUserId(UUID userId);
}
