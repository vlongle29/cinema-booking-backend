package com.example.CineBook.service;

import com.example.CineBook.dto.seattype.SeatTypeRequest;
import com.example.CineBook.dto.seattype.SeatTypeResponse;

import java.util.List;
import java.util.UUID;

public interface SeatTypeService {
    
    SeatTypeResponse create(SeatTypeRequest request);
    
    SeatTypeResponse update(UUID id, SeatTypeRequest request);
    
    SeatTypeResponse getById(UUID id);
    
    List<SeatTypeResponse> getAll();
    
    void delete(UUID id);
}
