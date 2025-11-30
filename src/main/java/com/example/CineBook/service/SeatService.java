package com.example.CineBook.service;

import com.example.CineBook.dto.seat.CreateSeatsRequest;
import com.example.CineBook.dto.seat.SeatResponse;

import java.util.List;
import java.util.UUID;

public interface SeatService {
    List<SeatResponse> createSeats(CreateSeatsRequest request);
    List<SeatResponse> getSeatsByRoom(UUID roomId);
    void deleteAllSeatsByRoom(UUID roomId);
}
