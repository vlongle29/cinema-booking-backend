package com.example.CineBook.dto.bookingproduct;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingProductCreateRequest {
    @NotNull(message = "Booking ID không được để trống")
    private UUID bookingId;
    
    @NotNull(message = "Product ID không được để trống")
    private UUID productId;
    
    @NotNull(message = "Số lượng không được để trống")
    private Integer quantity;
    
    private BigDecimal priceAtPurchase;
}
