package com.example.CineBook.model;

import com.example.CineBook.model.auditing.AuditingEntity;
import com.example.CineBook.common.constant.ShowtimeStatus;
import com.example.CineBook.common.constant.MovieFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "showtimes")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Showtime extends AuditingEntity {
    
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    
    @Column(name = "movie_id", nullable = false)
    private UUID movieId;
    
    @Column(name = "room_id", nullable = false)
    private UUID roomId;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "base_price")
    private BigDecimal basePrice;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MovieFormat format;
    
    @Enumerated(EnumType.STRING)
    @Column
    private ShowtimeStatus status;
}
