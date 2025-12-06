package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.position.PositionRequest;
import com.example.CineBook.dto.position.PositionResponse;
import com.example.CineBook.dto.position.PositionSearchDTO;
import com.example.CineBook.model.Position;
import com.example.CineBook.repository.irepository.PositionRepository;
import com.example.CineBook.repository.impl.PositionRepositoryImpl;
import com.example.CineBook.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public PositionResponse createPosition(PositionRequest request) {
        if (positionRepository.existsByCode(request.getCode())) {
            throw new BusinessException(MessageCode.POSITION_CODE_ALREADY_EXISTS);
        }

        Position position = Position.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Position saved = positionRepository.save(position);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PositionResponse updatePosition(UUID id, PositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.POSITION_NOT_FOUND));

        if (!position.getCode().equals(request.getCode()) && positionRepository.existsByCode(request.getCode())) {
            throw new BusinessException(MessageCode.POSITION_CODE_ALREADY_EXISTS);
        }

        position.setCode(request.getCode());
        position.setName(request.getName());
        position.setDescription(request.getDescription());

        Position updated = positionRepository.save(position);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.POSITION_NOT_FOUND));
        position.setIsDelete(true);
        positionRepository.save(position);
    }

    @Override
    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .filter(p -> !p.getIsDelete())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PageResponse<PositionResponse> searchPositions(PositionSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
        Page<Position> page = positionRepository.findAll(positionRepository.searchWithFilters(searchDTO), pageable);
        Page<PositionResponse> responsePage = page.map(this::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PositionResponse getPositionById(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.POSITION_NOT_FOUND));
        return toResponse(position);
    }

    private PositionResponse toResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .code(position.getCode())
                .name(position.getName())
                .description(position.getDescription())
                .build();
    }
}
