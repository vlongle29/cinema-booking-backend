package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.model.auditing.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Theater branch
 */

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "branches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@StaticMetamodel(Branch.class)
public class Branch extends AuditingEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String address;
    
    @Column
    private String city;
    
    @Column(name = "manager_id")
    private UUID managerId;
}
