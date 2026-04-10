package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.security.BranchSecurityHelper;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final BranchSecurityHelper branchSecurityHelper;

    @Override
    public List<RoomResponse> getAllRooms() {
        UUID branchId = branchSecurityHelper.getCurrentUserBranchId();
        
        List<Room> rooms;
        if (branchId == null) {
            // Super Admin: get all rooms
            rooms = roomRepository.findAll();
        } else {
            // Branch user: get only rooms of their branch
            rooms = roomRepository.findByBranchId(branchId);
        }
        
        return rooms.stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "rooms", key = "#result.id"),
            @CacheEvict(value = "rooms:by-branch", key = "#result.branchId")
    })
    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        // Auto-assign branchId from current user
        UUID branchId = branchSecurityHelper.getCurrentUserBranchId();
        
        // If user has branchId, use it; otherwise use from request (for Super Admin)
        if (branchId != null) {
            request.setBranchId(branchId);
        }
        
        if (!branchRepository.existsById(request.getBranchId())) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }

        boolean isExistRoom = roomRepository.existsByName(request.getName());
        if (isExistRoom) {
            throw new BusinessException(MessageCode.ROOM_ALREADY_EXISTS);
        }

        Room room = roomMapper.toEntity(request);
        Room saved = roomRepository.save(room);
        return roomMapper.toResponse(saved);
    }

    @Cacheable(value = "rooms", key = "#id")
    @Override
    public RoomResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    @Caching(evict = {
            @CacheEvict(value = "rooms", key = "#id"),
            @CacheEvict(value = "rooms:by-branch", allEntries = true),
            @CacheEvict(value = "rooms:seats", key = "#id")
    })
    @Override
    @Transactional
    public RoomResponse updateRoom(UUID id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));

        if (!branchRepository.existsById(request.getBranchId())) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }

        if (!request.getName().equals(room.getName()) && roomRepository.existsByName(request.getName())) {
            throw new BusinessException(MessageCode.ROOM_ALREADY_EXISTS);
        }

        roomMapper.updateEntity(request, room);
        Room updated = roomRepository.save(room);
        return roomMapper.toResponse(updated);
    }

    @Caching(evict = {
            @CacheEvict(value = "rooms", key = "#id"),
            @CacheEvict(value = "rooms:by-branch", allEntries = true),
            @CacheEvict(value = "rooms:seats", key = "#id")
    })
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

    @Caching(evict = {
            @CacheEvict(value = "rooms", key = "#id"),
            @CacheEvict(value = "rooms:by-branch", allEntries = true),
            @CacheEvict(value = "rooms:seats", key = "#id")
    })
    @Override
    @Transactional
    public void deleteRoomCascade(UUID id) {
        if (!roomRepository.existsById(id)) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        List<Seat> seats = seatRepository.findByRoomId(id);
        if (!seats.isEmpty()) {
            for (Seat seat : seats) {
                seat.setIsDelete(true);
            }
            seatRepository.saveAll(seats);
        }
        
        roomRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public RoomResponse restoreRoom(UUID id) {
        if (!roomRepository.existsById(id)) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        roomRepository.restoreById(id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        return roomMapper.toResponse(room);
    }

    @Cacheable(value = "rooms:by-branch", key = "#branchId")
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
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<Room> entityPage = roomRepository.searchWithFilters(searchDTO, pageable);
        Page<RoomResponse> responsePage = entityPage.map(roomMapper::toResponse);
        return PageResponse.of(responsePage);
    }
}
