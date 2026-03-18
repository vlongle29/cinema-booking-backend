package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_seat_showtime", columnList = "seat_id, showtime_id", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket extends AuditingEntity {
    
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;
    
    @Column(name = "seat_id", nullable = false)
    private UUID seatId;
    
    @Column
    private BigDecimal price;
    
    @Column(name = "showtime_id", nullable = false)
    private UUID showtimeId;
    
    @Column(name = "ticket_code", unique = true, length = 20)
    private String ticketCode;
    
    @Column(name = "qr_code_url")
    private String qrCodeUrl;
    
    @Column(name = "is_checked_in")
    private Boolean isCheckedIn = false;
}
