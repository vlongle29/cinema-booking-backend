package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedShowtimeItem {
    private UUID id;
    private String time;
    private String startTime;
    private String endTime;
}
