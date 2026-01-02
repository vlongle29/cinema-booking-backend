package com.example.CineBook.service.impl;

import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.seattype.SeatTypeRequest;
import com.example.CineBook.dto.seattype.SeatTypeResponse;
import com.example.CineBook.mapper.SeatTypeMapper;
import com.example.CineBook.model.SeatType;
import com.example.CineBook.repository.irepository.SeatRepository;
import com.example.CineBook.repository.irepository.SeatTypeRepository;
import com.example.CineBook.service.SeatTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatTypeServiceImpl implements SeatTypeService {
    
    private final SeatTypeRepository seatTypeRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeMapper seatTypeMapper;
    
    @Override
    @Transactional
    public SeatTypeResponse create(SeatTypeRequest request) {
        if (seatTypeRepository.existsByCode(request.getCode())) {
            throw new BusinessException(MessageCode.SEAT_TYPE_CODE_ALREADY_EXISTS);
        }
        
        SeatType seatType = seatTypeMapper.toEntity(request);
        SeatType saved = seatTypeRepository.save(seatType);
        return seatTypeMapper.toResponse(saved);
    }
    
    @Override
    @Transactional
    public SeatTypeResponse update(UUID id, SeatTypeRequest request) {
        SeatType seatType = seatTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TYPE_NOT_FOUND));
        
        if (!seatType.getCode().equals(request.getCode()) 
                && seatTypeRepository.existsByCode(request.getCode())) {
            throw new BusinessException(MessageCode.SEAT_TYPE_CODE_ALREADY_EXISTS);
        }
        
        seatTypeMapper.update(seatType, request);
        SeatType updated = seatTypeRepository.save(seatType);
        return seatTypeMapper.toResponse(updated);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SeatTypeResponse getById(UUID id) {
        SeatType seatType = seatTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TYPE_NOT_FOUND));
        return seatTypeMapper.toResponse(seatType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SeatTypeResponse> getAll() {
        return seatTypeRepository.findAll().stream()
                .map(seatTypeMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        if (!seatTypeRepository.existsById(id)) {
            throw new BusinessException(MessageCode.SEAT_TYPE_NOT_FOUND);
        }
        
        if (seatRepository.existsBySeatTypeId(id)) {
            throw new BusinessException(MessageCode.SEAT_TYPE_IN_USE);
        }
        
        seatTypeRepository.deleteById(id);
    }
}
