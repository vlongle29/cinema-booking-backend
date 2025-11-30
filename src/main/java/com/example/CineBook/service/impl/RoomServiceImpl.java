package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.room.RoomRequest;
import com.example.CineBook.dto.room.RoomResponse;
import com.example.CineBook.dto.room.RoomSearchDTO;
import com.example.CineBook.mapper.RoomMapper;
import com.example.CineBook.model.Room;
import com.example.CineBook.model.Seat;
import com.example.CineBook.repository.irepository.BranchRepository;
import com.example.CineBook.repository.irepository.RoomRepository;
import com.example.CineBook.repository.irepository.SeatRepository;

import java.util.List;
import java.util.stream.Collectors;
import com.example.CineBook.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BranchRepository branchRepository;
    private final RoomMapper roomMapper;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (!branchRepository.existsById(request.getBranchId())) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }

        Room room = roomMapper.toEntity(request);
        room.setCapacity(0);
        Room saved = roomRepository.save(room);
        return roomMapper.toResponse(saved);
    }

    @Override
    public RoomResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(UUID id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));

        if (!branchRepository.existsById(request.getBranchId())) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }

        roomMapper.updateEntity(request, room);
        Room updated = roomRepository.save(room);
        return roomMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRoom(UUID id) {
        if (!roomRepository.existsById(id)) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        long seatCount = seatRepository.countByRoomId(id);
        if (seatCount > 0) {
            throw new BusinessException(MessageCode.ROOM_HAS_SEAT);
        }
        
        roomRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public void deleteRoomCascade(UUID id) {
        if (!roomRepository.existsById(id)) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        List<Seat> seats = seatRepository.findByRoomId(id);
        if (!seats.isEmpty()) {
            List<UUID> seatIds = seats.stream().map(Seat::getId).collect(Collectors.toList());
            seatRepository.deleteAllById(seatIds);
        }
        
        roomRepository.softDeleteById(id);
    }

    @Override
    public List<RoomResponse> getRoomsByBranch(UUID branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }
        
        return roomRepository.findByBranchId(branchId).stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<RoomResponse> searchRooms(RoomSearchDTO searchDTO) {
        Page<Room> entityPage = roomRepository.findAllWithFilters(searchDTO);
        Page<RoomResponse> responsePage = entityPage.map(roomMapper::toResponse);
        return PageResponse.of(responsePage);
    }
}
