package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "booking_products")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingProduct extends AuditingEntity {
    
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column
    private Integer quantity;
    
    @Column(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;
}
