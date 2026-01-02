package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByRoomId(UUID roomId);
    boolean existsByRoomIdAndRowCharAndSeatNumber(UUID roomId, String rowChar, Integer seatNumber);
    long countByRoomId(UUID roomId);
    boolean existsBySeatTypeId(UUID seatTypeId);
}
