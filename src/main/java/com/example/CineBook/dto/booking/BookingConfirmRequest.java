package com.example.CineBook.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmRequest {
    
    @NotNull(message = "Showtime ID không được để trống")
    private UUID showtimeId;
    
    @NotEmpty(message = "Phải chọn ít nhất 1 ghế")
    private List<UUID> seatIds;
    
    private List<ProductItem> products;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItem {
        @NotNull(message = "Product ID không được để trống")
        private UUID productId;
        
        @NotNull(message = "Số lượng không được để trống")
        private Integer quantity;
        
        @NotNull(message = "Giá không được để trống")
        private BigDecimal priceAtPurchase;
    }
}
