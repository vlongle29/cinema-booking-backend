package com.example.CineBook.dto.seattype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatTypeResponse {
    
    private UUID id;
    private String code;
    private String name;
    private BigDecimal basePrice;
    private String description;
}
