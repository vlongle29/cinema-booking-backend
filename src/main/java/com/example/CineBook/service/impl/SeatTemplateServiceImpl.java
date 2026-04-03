package com.example.CineBook.service.impl;

import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.seat.SeatResponse;
import com.example.CineBook.dto.seattemplate.*;
import com.example.CineBook.mapper.SeatMapper;
import com.example.CineBook.mapper.SeatTemplateMapper;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.SeatTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatTemplateServiceImpl implements SeatTemplateService {

    private final SeatTemplateRepository seatTemplateRepository;
    private final SeatTemplateDetailRepository seatTemplateDetailRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final SeatTemplateMapper seatTemplateMapper;
    private final SeatMapper seatMapper;

    @Override
    @Transactional
    public SeatTemplateResponse createTemplate(CreateSeatTemplateRequest request) {
        // 1. Validate: Check if template name already exists
        if (seatTemplateRepository.existsByName(request.getName())) {
            throw new BusinessException(MessageCode.SEAT_TEMPLATE_NAME_ALREADY_EXISTS);
        }

        // 2. Convert request to entity
        SeatTemplate template = seatTemplateMapper.toEntity(request);

        // 3. Save to database
        SeatTemplate saved = seatTemplateRepository.save(template);

        // 4. Return response
        SeatTemplateResponse response = seatTemplateMapper.toResponse(saved);
        response.setSeats(List.of()); // No seats yet
        return response;
    }

    @Override
    @Transactional
    public SeatTemplateResponse addSeatsToTemplate(UUID templateId, AddSeatsToTemplateRequest request) {
        // 1. Check if template exists
        SeatTemplate template = seatTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TEMPLATE_NOT_FOUND));

        // 2. Validate seat types exist
        List<UUID> seatTypeIds = request.getSeats().stream()
                .map(SeatTemplateDetailRequest::getSeatTypeId)
                .distinct()
                .toList();

        for (UUID seatTypeId : seatTypeIds) {
            if (!seatTypeRepository.existsById(seatTypeId)) {
                throw new BusinessException(MessageCode.SEAT_TYPE_NOT_FOUND);
            }
        }

        // 3. Convert seats to entities
        List<SeatTemplateDetail> seatDetails = request.getSeats().stream()
                .map(seatRequest -> seatTemplateMapper.toDetailEntity(seatRequest, templateId))
                .toList();

        // 4. Save seats
        List<SeatTemplateDetail> savedSeats = seatTemplateDetailRepository.saveAll(seatDetails);

        // 5. Update totalSeats in template
        long totalSeats = seatTemplateDetailRepository.countByTemplateId(templateId);
        template.setTotalSeats((int) totalSeats);
        SeatTemplate updatedTemplate = seatTemplateRepository.save(template);

        // 6. Return updated template with seats
        SeatTemplateResponse response = seatTemplateMapper.toResponse(updatedTemplate);

        // Get seat details with seat type info
        List<SeatTemplateDetailResponse> seatResponses = savedSeats.stream()
                .map(detail -> {
                    SeatType seatType = seatTypeRepository.findById(detail.getSeatTypeId())
                            .orElse(null);
                    return seatTemplateMapper.toDetailResponse(detail, seatType);
                })
                .toList();

        response.setSeats(seatResponses);
        return response;
    }

    @Override
    public List<SeatTemplateResponse> getAllTemplates() {
        // 1. Get all templates (not deleted)
        List<SeatTemplate> templates = seatTemplateRepository.findByIsDeleteFalse();

        // 2. For each template, get its seats and map to response
        return templates.stream()
                .map(template -> {
                    // Get seats for this template
                    List<SeatTemplateDetail> seatDetails = seatTemplateDetailRepository.findByTemplateId(template.getId());

                    // Convert to response
                    SeatTemplateResponse response = seatTemplateMapper.toResponse(template);

                    // Map seat details with seat type info
                    List<SeatTemplateDetailResponse> seatResponses = seatDetails.stream()
                            .map(detail -> {
                                SeatType seatType = seatTypeRepository.findById(detail.getSeatTypeId())
                                        .orElse(null);
                                return seatTemplateMapper.toDetailResponse(detail, seatType);
                            })
                            .toList();

                    response.setSeats(seatResponses);
                    return response;
                })
                .toList();
    }

    @Override
    public SeatTemplateResponse getTemplateById(UUID templateId) {
        // 1. Find template by id
        Optional<SeatTemplate> template = seatTemplateRepository.findById(templateId);

        // 2. Get all seats of template
        List<SeatTemplateDetail> seatDetails = seatTemplateDetailRepository.findByTemplateId(templateId);

        // 3. Get seat types
        List<UUID> seatTypeIds = seatDetails.stream().map(SeatTemplateDetail::getSeatTypeId).distinct().toList();
        List<SeatType> seatTypes = seatTypeRepository.findAllById(seatTypeIds);

        // 4. Map to response with full details
        return template.map(t -> {
            SeatTemplateResponse response = seatTemplateMapper.toResponse(t);
            List<SeatTemplateDetailResponse> seatResponses = seatDetails.stream()
                    .map(detail -> {
                        SeatType seatType = seatTypes.stream()
                                .filter(st -> st.getId().equals(detail.getSeatTypeId()))
                                .findFirst()
                                .orElse(null);
                        return seatTemplateMapper.toDetailResponse(detail, seatType);
                    })
                    .toList();
            response.setSeats(seatResponses);
            return response;
        }).orElseThrow(() -> new BusinessException(MessageCode.SEAT_TEMPLATE_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID templateId) {
        // 1. Check if template exists
        SeatTemplate template = seatTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TEMPLATE_NOT_FOUND));

        // 2. Soft delete template (set isDelete = true)
        template.setIsDelete(true);
        seatTemplateRepository.save(template);

        // 3. Delete all seats of template
        seatTemplateDetailRepository.deleteByTemplateId(templateId);
    }

    @Override
    @Transactional
    public List<SeatResponse> applyTemplateToRoom(UUID templateId, UUID roomId) {
        // 1. Check if template exists
        SeatTemplate template = seatTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TEMPLATE_NOT_FOUND));

        // 2. Check if room exists
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));

        // 3. Check if room already has seats
        long existingSeatCount = seatRepository.countByRoomId(roomId);
        if (existingSeatCount > 0) {
            throw new BusinessException(MessageCode.ROOM_HAS_SEATS);
        }

        // 4. Get all seats from template
        List<SeatTemplateDetail> templateSeats = seatTemplateDetailRepository.findByTemplateId(templateId);
        if (templateSeats.isEmpty()) {
            throw new BusinessException(MessageCode.SEAT_TEMPLATE_NOT_FOUND);
        }

        // 5. Clone seats from template to room (create new Seat entities)
        List<Seat> newSeats = templateSeats.stream()
                .map(templateSeat -> Seat.builder()
                        .roomId(roomId)
                        .rowChar(templateSeat.getRowChar())
                        .seatNumber(templateSeat.getSeatNum())
                        .seatTypeId(templateSeat.getSeatTypeId())
                        .build())
                .toList();

        // 6. Save all seats
        List<Seat> savedSeats = seatRepository.saveAll(newSeats);

        // 7. Update room capacity
        room.setCapacity(savedSeats.size());
        roomRepository.save(room);

        // 8. Return created seats with seat type info
        return savedSeats.stream()
                .map(seat -> {
                    SeatType seatType = seatTypeRepository.findById(seat.getSeatTypeId())
                            .orElse(null);
                    return seatMapper.toResponse(seat, seatType);
                })
                .toList();
    }
}
