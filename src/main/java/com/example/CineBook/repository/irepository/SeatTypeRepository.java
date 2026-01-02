package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, UUID> {
    
    Optional<SeatType> findByCode(String code);
    
    boolean existsByCode(String code);
}
