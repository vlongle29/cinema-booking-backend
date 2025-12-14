package com.example.CineBook.scheduler;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.model.Booking;
import com.example.CineBook.repository.irepository.BookingProductRepository;
import com.example.CineBook.repository.irepository.BookingRepository;
import com.example.CineBook.repository.irepository.TicketRepository;
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
                    ticketRepository.deleteByBookingId(booking.getId());
                    bookingProductRepository.deleteByBookingId(booking.getId());
                    booking.setStatus(BookingStatus.EXPIRED);
                    bookingRepository.save(booking);
                    log.info("Cancelled expired booking: {}", booking.getId());
                } catch (Exception e) {
                    log.error("Error cancelling booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }
    }
}
