package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.RoomType;
import com.example.CineBook.model.auditing.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.StaticMetamodel;
import lombok.*;

import java.util.UUID;

@StaticMetamodel(Room.class)
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room extends AuditingEntity {
    
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    
    @Column
    private String name;

    /**
     * Total number of seats. Used to calculate occupancy %
     */
    @Column
    private Integer capacity;

    /**
     * Type room (2D, 3D, IMAX). Support filtering showtimes by experience
     */
    @Enumerated(EnumType.STRING)
    @Column
    private RoomType type;
}
