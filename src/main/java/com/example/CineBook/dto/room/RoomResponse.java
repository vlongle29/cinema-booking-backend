package com.example.CineBook.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
