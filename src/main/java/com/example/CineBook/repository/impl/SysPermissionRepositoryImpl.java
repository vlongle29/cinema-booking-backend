package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.sysPermission.SysPermissionSearchDTO;
import com.example.CineBook.model.*;

import com.example.CineBook.repository.base.BaseRepositoryImpl;
import com.example.CineBook.repository.custom.SysPermissionRepositoryCustom;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
public class SysPermissionRepositoryImpl extends BaseRepositoryImpl<SysPermission, SysPermissionSearchDTO> implements SysPermissionRepositoryCustom {

    public SysPermissionRepositoryImpl() {
        super(SysPermission.class);
    }

    @Override
    public List<String> getPermissionInfoByUserId(UUID userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery query = cb.createQuery(String.class);

        Root<SysUserRole> userRoleRoot = query.from(SysUserRole.class);
        Root<SysRolePermission> rolePermissionRoot = query.from(SysRolePermission.class);
        Root<SysPermission> permissionRoot = query.from(SysPermission.class);

        query.select(permissionRoot.get(SysPermission_.permission)).distinct(true);

        query.where(cb.and(
                cb.equal(userRoleRoot.get(SysUserRole_.userId), userId),
                cb.equal(userRoleRoot.get("roleId"), rolePermissionRoot.get(SysRolePermission_.ROLE_ID)),
                cb.equal(rolePermissionRoot.get(SysRolePermission_.PERMISSION_ID), permissionRoot.get(SysPermission_.ID))
        ));
        return entityManager.createQuery(query).getResultList();
    }
}
