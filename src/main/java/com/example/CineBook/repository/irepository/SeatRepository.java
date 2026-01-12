package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByRoomId(UUID roomId);
    boolean existsByRoomIdAndRowCharAndSeatNumber(UUID roomId, String rowChar, Integer seatNumber);
    long countByRoomId(UUID roomId);
    boolean existsBySeatTypeId(UUID seatTypeId);

    @Query(value = "SELECT st.name FROM seat_types st JOIN seats s ON s.seat_type_id = st.id WHERE s.id = CAST(:seatId AS uuid)", nativeQuery = true)
    String getSeatTypeNameById(UUID seatId);
}