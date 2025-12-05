package com.example.CineBook.service.impl;

import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.seat.CreateSeatsRequest;
import com.example.CineBook.dto.seat.SeatRequest;
import com.example.CineBook.dto.seat.SeatResponse;
import com.example.CineBook.mapper.SeatMapper;
import com.example.CineBook.model.Room;
import com.example.CineBook.model.Seat;
import com.example.CineBook.repository.irepository.RoomRepository;
import com.example.CineBook.repository.irepository.SeatRepository;
import com.example.CineBook.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final SeatMapper seatMapper;

    @Override
    @Transactional
    public List<SeatResponse> createSeats(CreateSeatsRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));

        // Check if seat exists
        for (SeatRequest seatReq : request.getSeats()) {
            String rowChar = extractRowChar(seatReq.getSeatNumber());
            Integer seatNum = extractSeatNumber(seatReq.getSeatNumber());
            
            if (seatRepository.existsByRoomIdAndRowCharAndSeatNumber(request.getRoomId(), rowChar, seatNum)) {
                throw new BusinessException(MessageCode.SEAT_ALREADY_EXISTS);
            }
        }

        // Assign roomId to seat
        List<Seat> seats = request.getSeats().stream()
                .map(seatReq -> seatMapper.toEntity(seatReq, request.getRoomId()))
                .collect(Collectors.toList());

        List<Seat> saved = seatRepository.saveAll(seats);

        // Set capacity for room
        room.setCapacity((int) seatRepository.countByRoomId(request.getRoomId()));
        roomRepository.save(room);
        
        return saved.stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponse> getSeatsByRoom(UUID roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }

        return seatRepository.findByRoomId(roomId).stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllSeatsByRoom(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));

        List<Seat> seats = seatRepository.findByRoomId(roomId);
        if (!seats.isEmpty()) {
            List<UUID> seatIds = seats.stream().map(Seat::getId).collect(Collectors.toList());
            seatRepository.deleteAllById(seatIds);
            
            room.setCapacity(0);
            roomRepository.save(room);
        }
    }

    private String extractRowChar(String seatNumber) {
        return seatNumber.replaceAll("[0-9]", "");
    }

    private Integer extractSeatNumber(String seatNumber) {
        return Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
    }
}
