package com.example.CineBook.dto.seat;

import com.example.CineBook.dto.seattype.SeatTypeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    
    private UUID id;
    private String seatNumber;
    private UUID seatTypeId;
    private SeatTypeResponse seatType;
    private UUID roomId;
}
