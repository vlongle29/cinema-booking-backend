package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.model.auditing.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.*;

@StaticMetamodel(SysPermission.class)
@EntityListeners(AuditingEntityListener.class)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class SysPermission extends AuditingEntity {

    @Column(unique = true, length = 100, nullable = false)
    private String permission;

    @Column(length = 100, nullable = false)
    private String code;

    @Column(length = 500, nullable = false)
    private String description;

    @Column(length = 100, nullable = false)
    private String systemFlag;
}
