package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seat_types")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatType extends AuditingEntity {
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    
    @Column(length = 500)
    private String description;
}
