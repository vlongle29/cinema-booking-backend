package com.example.CineBook.scheduler;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.model.Booking;
import com.example.CineBook.model.Ticket;
import com.example.CineBook.repository.irepository.BookingRepository;
import com.example.CineBook.repository.irepository.TicketRepository;
import com.example.CineBook.service.SeatHoldService;
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
    private final SeatHoldService seatHoldService;
    private final TicketRepository ticketRepository;
    private final SeatWebSocketService seatWebSocketService;

    private static final int DRAFT_EXPIRATION_CHECK_INTERVAL = 60000; // 1 minute
    private static final int PAYMENT_TIMEOUT_CHECK_INTERVAL = 300000; // 5 minutes
    private static final int PAYMENT_TIMEOUT_MINUTES = 30; // 30 minutes

    @Scheduled(fixedRate = DRAFT_EXPIRATION_CHECK_INTERVAL)
    public void cancelExpiredDraftBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndExpiredAtBefore(BookingStatus.DRAFT, now);

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired DRAFT bookings to cancel", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                try {
                    // Release seats from Redis + update status to EXPIRED
                    // Note: DRAFT bookings don't have Tickets in DB yet (only Redis holds)
                    // Tickets are created during checkout(), so no DB cleanup needed here
                    // We keep the Booking record with EXPIRED status for audit/statistics
                    seatHoldService.releaseSeats(booking.getId());

                    log.info("Cancelled expired DRAFT booking: {}", booking.getId());
                } catch (Exception e) {
                    log.error("Error cancelling DRAFT booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }
    }

    @Scheduled(fixedRate = PAYMENT_TIMEOUT_CHECK_INTERVAL)
    @Transactional
    public void cancelPendingPaymentBookings() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);

        List<Booking> pendingBookings = bookingRepository
                .findByStatusAndUpdateTimeBefore(BookingStatus.PENDING_PAYMENT, timeout);

        if (!pendingBookings.isEmpty()) {
            log.info("Found {} PENDING_PAYMENT bookings to cancel (payment timeout)", pendingBookings.size());

            for (Booking booking : pendingBookings) {
                try {
                    List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());

                    ticketRepository.deleteByBookingId(booking.getId());

                    booking.setStatus(BookingStatus.CANCELLED);
                    booking.setCancellationReason("Payment timeout - No payment received within " + PAYMENT_TIMEOUT_MINUTES + " minutes");
                    booking.setCancelledAt(LocalDateTime.now());
                    bookingRepository.save(booking);

                    for (Ticket ticket : tickets) {
                        seatWebSocketService.notifySeatReleased(
                                ticket.getShowtimeId(),
                                ticket.getSeatId()
                        );
                    }

                    log.info("Cancelled PENDING_PAYMENT booking: {} with {} seats released",
                            booking.getId(), tickets.size());
                } catch (Exception e) {
                    log.error("Error cancelling PENDING_PAYMENT booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }
    }
}
