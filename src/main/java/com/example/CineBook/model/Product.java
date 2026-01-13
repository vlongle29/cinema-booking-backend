package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AuditingEntity {
    
    @Column
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column
    private BigDecimal price;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column
    private ProductCategory category;
    
    @Column(name = "is_active")
    private Boolean isActive;
}
