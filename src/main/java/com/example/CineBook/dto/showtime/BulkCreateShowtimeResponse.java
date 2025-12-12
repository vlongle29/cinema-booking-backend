package com.example.CineBook.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateShowtimeResponse {
    
    private Integer totalRequested;
    private Integer created;
    private Integer skipped;
    private List<ConflictInfo> conflicts;
    private List<ShowtimeResponse> showtimes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictInfo {
        private LocalDate date;
        private LocalTime time;
        private String reason;
    }
}
