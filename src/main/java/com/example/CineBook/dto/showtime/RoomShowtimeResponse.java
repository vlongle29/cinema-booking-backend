package com.example.CineBook.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomShowtimeResponse {
    
    private Map<LocalDate, List<ShowtimeSlot>> showtimesByDate;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowtimeSlot {
        private UUID id;
        private String movieTitle;
        private LocalTime startTime;
        private LocalTime endTime;
        private String format;
    }
}
