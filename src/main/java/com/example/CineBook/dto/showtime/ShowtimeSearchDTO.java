package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSearchDTO extends SearchBaseDto {
    private UUID movieId;
    private UUID branchId;
    private UUID roomId;
    private UUID cityId;
    private LocalDate date;
    private MovieFormat format;
}
