package com.example.CineBook.service.impl;

import com.example.CineBook.service.SysUserRoleService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SysUserRoleServiceImpl implements SysUserRoleService {

    @Override
    public Optional<UUID> findRoleByUserId(UUID userId) {
        // TODO: Implement logic to find role by user ID
        return Optional.empty();
    }
}
