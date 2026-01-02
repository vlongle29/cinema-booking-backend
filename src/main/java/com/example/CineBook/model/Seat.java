package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.SeatStatus;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "seats")
@Data
@EqualsAndHashCode(callSuper = false)
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
    @Column(name = "seat_type_id", nullable = false)
    private UUID seatTypeId;

    /**
     * Physical state (AVAILABLE, BROKEN)
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column
    private SeatStatus status = SeatStatus.AVAILABLE;
}
