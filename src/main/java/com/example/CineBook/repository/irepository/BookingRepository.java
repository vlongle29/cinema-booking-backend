package com.example.CineBook.repository.irepository;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomerId(UUID customerId);
    
    List<Booking> findByStatusAndExpiredAtBefore(BookingStatus status, LocalDateTime expiredAt);
}
