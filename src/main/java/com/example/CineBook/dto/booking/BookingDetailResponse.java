package com.example.CineBook.dto.booking;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.constant.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    
    // 1. Thông tin chung
    private UUID bookingId;
    private String bookingCode;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    
    // 2. Chi tiết suất chiếu
    private ShowtimeInfo showtime;
    
    // 3. Chi tiết ghế & Dịch vụ
    private List<SeatInfo> seats;
    private List<ProductInfo> products;
    
    // 4. Thông tin thanh toán
    private PaymentInfo payment;
    
    // 5. Thông tin khách hàng
    private CustomerInfo customer;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowtimeInfo {
        private String movieTitle;
        private String branchName;
        private String roomName;
        private LocalDateTime startTime;
        private String cityName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private String seatNumber;
        private String seatType;
        private BigDecimal price;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String productName;
        private Integer quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal totalPrice;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private BigDecimal totalTicketPrice;
        private BigDecimal totalFoodPrice;
        private BigDecimal discountAmount;
        private BigDecimal finalAmount;
        private PaymentMethod paymentMethod;
        private String transactionCode;
        private LocalDateTime paymentTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String name;
        private String email;
        private String phone;
    }
}
