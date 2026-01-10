package com.example.CineBook.scheduler;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.model.Booking;
import com.example.CineBook.model.Ticket;
import com.example.CineBook.repository.irepository.BookingProductRepository;
import com.example.CineBook.repository.irepository.BookingRepository;
import com.example.CineBook.repository.irepository.TicketRepository;
import com.example.CineBook.websocket.service.SeatWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final BookingProductRepository bookingProductRepository;
    private final SeatWebSocketService seatWebSocketService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndExpiredAtBefore(BookingStatus.DRAFT, now);

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired bookings to cancel", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                try {
                    // Get tickets list before delete to broadcast
                    List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());

                    // Delete tickets and products
                    ticketRepository.deleteByBookingId(booking.getId());
                    bookingProductRepository.deleteByBookingId(booking.getId());

                    // Update status
                    booking.setStatus(BookingStatus.EXPIRED);
                    bookingRepository.save(booking);

                    // Broadcast WebSocket: Notification seat released
                    for (Ticket ticket : tickets) {
                        seatWebSocketService.notifySeatReleased(ticket.getShowtimeId(), ticket.getSeatId());
                    }
                    log.info("Cancelled expired booking: {} with {} seats released",
                            booking.getId(), tickets.size());
                } catch (Exception e) {
                    log.error("Error cancelling booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }
    }
}
