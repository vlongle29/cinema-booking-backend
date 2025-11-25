package com.example.CineBook.repository.impl;

import com.example.CineBook.dto.sysPermission.SysPermissionSearchDTO;
import com.example.CineBook.model.SysPermission;

import com.example.CineBook.model.SysRolePermission;
import com.example.CineBook.model.SysUserRole;
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

        query.select(permissionRoot.get("permission")).distinct(true);

        query.where(cb.and(
                cb.equal(userRoleRoot.get("userId"), userId),
                cb.equal(userRoleRoot.get("roleId"), rolePermissionRoot.get("roleId")),
                cb.equal(rolePermissionRoot.get("permissionId"), permissionRoot.get("id"))
        ));
        return entityManager.createQuery(query).getResultList();
    }
}
