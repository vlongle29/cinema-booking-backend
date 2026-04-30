package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "seat_templates_detail")
@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatTemplateDetail extends AuditingEntity {
    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "row_char", nullable = false, length = 2)
    private String rowChar;

    @Column(name = "row_index", nullable = false)
    private Integer rowIndex;

    @Column(name = "column_index", nullable = false)
    private Integer columnIndex;

    @Column(name = "seat_num", nullable = false)
    private Integer seatNum;

    @Column(name = "seat_type_id", nullable = false)
    private UUID seatTypeId;

    @Column(name = "is_aisle", nullable = false)
    private Boolean isAisle = false;
}
