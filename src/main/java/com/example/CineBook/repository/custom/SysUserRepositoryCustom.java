package com.example.CineBook.repository.custom;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.dto.auth.AuthorityProjection;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface SysUserRepositoryCustom {
    int updateLockStatusForIds(List<UUID> ids, LockFlag lockFlag);

    List<AuthorityProjection> findAllAuthoritiesByUserId(UUID userId);
}
