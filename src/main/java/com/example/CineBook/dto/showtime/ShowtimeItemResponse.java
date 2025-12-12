package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  ShowtimeItemResponse {
    private UUID showtimeId;
    private LocalDateTime startTime;
    private MovieFormat format;
    private String roomName;
    private Integer availableSeats;
}
