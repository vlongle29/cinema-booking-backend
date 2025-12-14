package com.example.CineBook.dto.bookingproduct;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BookingProductResponse {
    private UUID id;
    private UUID bookingId;
    private UUID productId;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
}
