package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.SeatTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SeatTemplateRepository extends JpaRepository<SeatTemplate, UUID> {
    
    List<SeatTemplate> findByIsDeleteFalse();

    boolean existsByName(String name);

    @Query("SELECT st FROM SeatTemplate st WHERE st.isDelete = false AND st.name LIKE %:name%")
    List<SeatTemplate> searchByName(String name);
}
