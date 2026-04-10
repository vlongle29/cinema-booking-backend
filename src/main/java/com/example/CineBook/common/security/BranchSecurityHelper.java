package com.example.CineBook.common.security;

import com.example.CineBook.repository.irepository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BranchSecurityHelper {
    
    private final SysUserRepository sysUserRepository;
    
    /**
     * Get branchId of current user from database.
     * Returns null if user is Super Admin or branchId is not set.
     */
    public UUID getCurrentUserBranchId() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return sysUserRepository.findById(userId)
                .map(user -> user.getBranchId())
                .orElse(null);
    }
    
    /**
     * Check if current user is Super Admin (branchId is null).
     */
    public boolean isSuperAdmin() {
        return getCurrentUserBranchId() == null;
    }
}
