package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByBookingId(UUID bookingId);
    List<Ticket> findByShowtimeId(UUID showtimeId);
    void deleteByBookingId(UUID bookingId);
    boolean existsBySeatIdAndShowtimeId(UUID seatId, UUID showtimeId);
    Optional<Ticket> findByTicketCode(String ticketCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.seatId = :seatId AND t.showtimeId = :showtimeId")
    Optional<Ticket> findBySeatAndShowtimeWithLock(@Param("seatId") UUID seatId, @Param("showtimeId") UUID showtimeId);

}
