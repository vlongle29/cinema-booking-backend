package com.example.CineBook.service;

import com.example.CineBook.dto.seat.SeatHoldData;
import com.example.CineBook.dto.seat.SeatHoldRequest;

import java.util.List;
import java.util.UUID;

public interface SeatHoldService {
    void holdSeats(UUID bookingId, SeatHoldRequest request); // Hold 1 seat
    void releaseSeat(UUID bookingId, UUID showtimeId, UUID seatId); // Release 1 seat
    void releaseSeats(UUID bookingId); // Release all seats of booking
    List<SeatHoldData> getHoldsByBooking(UUID bookingId);
}
