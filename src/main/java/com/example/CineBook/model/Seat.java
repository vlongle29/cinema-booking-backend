package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.SeatStatus;
import com.example.CineBook.common.constant.SeatType;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@StaticMetamodel(Seat.class)
public class Seat extends AuditingEntity {
    
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    /**
     * Rows (A, B, C...).
     */
    @Column(name = "row_char", length = 2)
    private String rowChar;

    /**
     * Seat numbers (1, 2, 3...). Combine them into A1, A2.
     */
    @Column(name = "seat_number")
    private Integer seatNumber;

    /**
     * Type seat (STANDARD, VIP, COUPLE). Decide on the basic price
     */
    @Enumerated(EnumType.STRING)
    @Column
    private SeatType type;

    /**
     * Physical state (AVAILABLE, BROKEN)
     */
    @Enumerated(EnumType.STRING)
    @Column
    private SeatStatus status = SeatStatus.AVAILABLE;
}
