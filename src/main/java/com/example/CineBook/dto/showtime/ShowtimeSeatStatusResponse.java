package com.example.CineBook.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSeatStatusResponse {
    
    private UUID showtimeId;
    private UUID roomId;
    private String roomName;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer bookedSeats;
    private Integer heldSeats;
    private List<SeatStatus> seats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatStatus {
        private UUID seatId;
        private Integer seatNumber;
        private String rowChar;
        private String seatType;
        private BigDecimal price;
        private String status; // AVAILABLE, BOOKED, HELD
        private UUID heldByBookingId; // Only if status = HELD
    }
}
