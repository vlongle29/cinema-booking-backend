package com.example.CineBook.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TicketItemRequest {
    @NotNull(message = "Seat ID không được để trống")
    private UUID seatId;
    
    @NotNull(message = "Giá vé không được để trống")
    private BigDecimal price;
}
