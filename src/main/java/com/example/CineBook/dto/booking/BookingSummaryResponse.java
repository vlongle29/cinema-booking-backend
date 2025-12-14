package com.example.CineBook.dto.booking;

import com.example.CineBook.dto.bookingproduct.BookingProductResponse;
import com.example.CineBook.dto.ticket.TicketResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingSummaryResponse {
    private UUID bookingId;
    private UUID showtimeId;
    private List<TicketResponse> tickets;
    private List<BookingProductResponse> products;
    private BigDecimal totalTicketPrice;
    private BigDecimal totalFoodPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}
