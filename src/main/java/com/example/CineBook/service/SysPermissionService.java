package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.sysPermission.SysPermissionRequest;
import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysPermission.SysPermissionSearchDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SysPermissionService {
    SysPermissionResponse create(SysPermissionRequest request);

    SysPermissionResponse update(SysPermissionRequest request);

    void delete(UUID id);

    @Transactional
    void deleteBatch(List<UUID> ids);

    Optional<SysPermissionResponse> findById(UUID id);


    PageResponse<SysPermissionResponse> search(SysPermissionSearchDTO searchDTO);

    List<String> getPermissionInfoByUserId(UUID userId);
}
