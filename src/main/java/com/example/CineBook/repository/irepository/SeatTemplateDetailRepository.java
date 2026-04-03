package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.SeatTemplateDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatTemplateDetailRepository extends JpaRepository<SeatTemplateDetail, UUID> {
    
    List<SeatTemplateDetail> findByTemplateId(UUID templateId);
    
    void deleteByTemplateId(UUID templateId);
    
    long countByTemplateId(UUID templateId);
}
