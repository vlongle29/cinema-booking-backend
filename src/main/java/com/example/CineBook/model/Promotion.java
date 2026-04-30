package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion extends AuditingEntity {
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;
    
    @Column(name = "discount_value")
    private BigDecimal discountValue;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
}
