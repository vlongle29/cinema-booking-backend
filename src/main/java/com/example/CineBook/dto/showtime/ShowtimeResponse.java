package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.dto.branch.BranchResponse;
import com.example.CineBook.dto.movie.MovieResponse;
import com.example.CineBook.dto.room.RoomResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {
    
    private UUID id;
    private UUID movieId;
    private UUID roomId;
    private UUID branchId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private MovieFormat format;
    private String status;
    
    private MovieResponse movie;
    private RoomResponse room;
    private BranchResponse branch;
}
