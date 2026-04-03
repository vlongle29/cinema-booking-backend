package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "seat_template")
@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatTemplate extends AuditingEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "rows", nullable = false)
    private Integer rows;

    @Column(name = "columns", nullable = false)
    private Integer columns;
}
