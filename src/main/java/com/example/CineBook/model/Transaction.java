package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends AuditingEntity {
    
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;
    
    @Column(name = "transaction_code", unique = true)
    private String transactionCode;
    
    @Column(name = "payment_gateway")
    private String paymentGateway;
    
    @Column
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column
    private TransactionStatus status;
    
    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;
    
    @Column(name = "raw_response_data", columnDefinition = "TEXT")
    private String rawResponseData;
}
