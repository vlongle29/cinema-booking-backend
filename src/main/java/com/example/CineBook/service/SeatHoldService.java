package com.example.CineBook.service;

import com.example.CineBook.dto.seat.*;

import java.util.List;
import java.util.UUID;

public interface SeatHoldService {
    void holdSeats(UUID bookingId, SeatHoldRequest request); // Hold 1 seat
    void releaseSeat(UUID bookingId, UUID showtimeId, UUID seatId); // Release 1 seat
    void releaseSeats(UUID bookingId); // Release all seats + mark EXPIRED (for cancel/timeout)
    void clearHolds(UUID bookingId); // Clear Redis holds only (for successful checkout)
    List<SeatHoldData> getHoldsByBooking(UUID bookingId);
    
    // New methods for Option 3
    SeatAvailabilityResponse checkAvailability(SeatAvailabilityRequest request);
    SeatPreHoldResponse preHoldSeats(SeatPreHoldRequest request);
    boolean isSeatHeld(UUID seatId, UUID showtimeId);
}
