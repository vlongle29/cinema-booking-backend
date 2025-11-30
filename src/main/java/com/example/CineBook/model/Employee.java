package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.model.auditing.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@StaticMetamodel(Employee.class)
public class Employee extends AuditingEntity {

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;
    
    @Column(name = "branch_id", nullable = false)
    private UUID branchId; // Identify which theater the employee belongs to?
    
    @Column(name = "employee_code", unique = true)
    private String employeeCode; // Ex: NV001

    @Column(name = "position")
    private String position;
    
    @Column
    private BigDecimal salary;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
}
