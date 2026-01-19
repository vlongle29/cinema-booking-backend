package com.example.CineBook.repository.irepository;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.booking.BookingResponse;
import com.example.CineBook.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomerId(UUID customerId);
    
    List<Booking> findByStatusAndExpiredAtBefore(BookingStatus status, LocalDateTime expiredAt);
    
    List<Booking> findByStatusAndUpdateTimeBefore(BookingStatus status, LocalDateTime updatedAt);
    
    @Query("SELECT b FROM Booking b WHERE b.customerId = :customerId AND b.isDelete = false")
    Page<Booking> findByCustomerIdOrderByBookingDateDesc(@Param("customerId") UUID customerId, Pageable pageable);
}
