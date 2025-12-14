package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.BookingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingProductRepository extends JpaRepository<BookingProduct, UUID> {
    List<BookingProduct> findByBookingId(UUID bookingId);
    void deleteByBookingId(UUID bookingId);
}
