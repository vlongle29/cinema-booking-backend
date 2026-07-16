package com.example.CineBook.scheduler;

import com.example.CineBook.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowtimeScheduler {
    
    private final ShowtimeService showtimeService;
    
    @Scheduled(cron = "0 */30 * * * *") // Chạy mỗi 30 phút
    public void closeExpiredShowtimes() {
        int updated = showtimeService.closeExpiredShowtimes();
        
        if (updated > 0) {
            log.info("Closed {} expired showtimes", updated);
        }
    }
}
