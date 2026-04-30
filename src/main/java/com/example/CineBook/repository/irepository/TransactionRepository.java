package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByBookingId(UUID bookingId);
    boolean existsByBookingId(UUID bookingId);
}
