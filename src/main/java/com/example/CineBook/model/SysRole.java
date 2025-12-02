package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.model.auditing.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.*;

@StaticMetamodel(SysRole.class)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "sys_role")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class SysRole extends AuditingEntity {
    @Column(unique = true, length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String code;

    @Column(length = 500, nullable = false)
    private String description;

    @Column(length = 50)
    private String systemFlag;

    /**
     * Độ ưu tiên của vai trò. Số càng cao thì độ ưu tiên càng thấp.
     * Ví dụ: Giám đốc = 1, Trưởng phòng = 2, Nhân viên = 3
     */
    @Column(nullable = true)
    private Integer priority;

}

