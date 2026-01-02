package com.example.CineBook.websocket.service;

import com.example.CineBook.websocket.dto.SeatStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatWebSocketService {

    private final WebSocketSessionManager sessionManager;

    public void notifySeatSelected(UUID showtimeId, UUID seatId, UUID bookingId, LocalDateTime expiresAt) {
        SeatStatusMessage message = SeatStatusMessage.builder()
                .type("SEAT_SELECTED")
                .showtimeId(showtimeId)
                .seatId(seatId)
                .bookingId(bookingId)
                .expiresAt(expiresAt)
                .build();

        sessionManager.broadcast("showtime", showtimeId, message);
        log.info("Notified SEAT_SELECTED: showtime={}, seat={}", showtimeId, seatId);
    }

    public void notifySeatBooked(UUID showtimeId, UUID seatId, UUID bookingId) {
        SeatStatusMessage message = SeatStatusMessage.builder()
                .type("SEAT_BOOKED")
                .showtimeId(showtimeId)
                .seatId(seatId)
                .bookingId(bookingId)
                .build();

        sessionManager.broadcast("showtime", showtimeId, message);
        log.info("Notified SEAT_BOOKED: showtime={}, seat={}", showtimeId, seatId);
    }

    public void notifySeatReleased(UUID showtimeId, UUID seatId) {
        SeatStatusMessage message = SeatStatusMessage.builder()
                .type("SEAT_RELEASED")
                .showtimeId(showtimeId)
                .seatId(seatId)
                .build();

        sessionManager.broadcast("showtime", showtimeId, message);
        log.info("Notified SEAT_RELEASED: showtime={}, seat={}", showtimeId, seatId);
    }
}
