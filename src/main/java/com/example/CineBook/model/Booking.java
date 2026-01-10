package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.constant.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends AuditingEntity {
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    @Column(name = "staff_id")
    private UUID staffId;
    
    @Column(name = "showtime_id", nullable = false)
    private UUID showtimeId;
    
    @Column(name = "promotion_id")
    private UUID promotionId;
    
    @Column(name = "total_ticket_price")
    private BigDecimal totalTicketPrice;
    
    @Column(name = "total_food_price")
    private BigDecimal totalFoodPrice;
    
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    
    @Column(name = "final_amount")
    private BigDecimal finalAmount;
    
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @Enumerated(EnumType.STRING)
    @Column
    private BookingStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
