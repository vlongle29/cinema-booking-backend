package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "position")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position extends AuditingEntity {
    
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
}
