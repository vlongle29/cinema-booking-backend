package com.example.CineBook.model;

import jakarta.persistence.*;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@StaticMetamodel(SysRolePermission.class)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sys_role_permission", uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "permission_id"})})
public class SysRolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;
}
