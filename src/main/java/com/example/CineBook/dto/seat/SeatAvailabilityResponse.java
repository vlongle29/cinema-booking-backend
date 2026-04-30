package com.example.CineBook.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    
    private boolean allAvailable;
    private List<UUID> availableSeats;
    private List<UUID> unavailableSeats;
    private String message;
}
