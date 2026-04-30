package com.example.CineBook.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    
    private UUID id;
    private String name;
    private Integer totalSeats;
    private UUID branchId;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Instant createTime;
    private Instant updateTime;

}
