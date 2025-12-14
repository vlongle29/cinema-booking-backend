package com.example.CineBook.dto.bookingproduct;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingProductItemRequest {
    @NotNull(message = "Product ID không được để trống")
    private UUID productId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    @NotNull(message = "Giá không được để trống")
    private BigDecimal priceAtPurchase;
}
