package com.example.CineBook.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSearchDTO {
    private UUID movieId;
    private UUID branchId;
    private UUID roomId;
    private LocalDate date;
    private Integer page = 0;
    private Integer size = 10;
}
