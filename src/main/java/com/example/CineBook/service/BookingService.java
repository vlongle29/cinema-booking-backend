package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.booking.*;
import com.example.CineBook.dto.bookingproduct.BookingProductBatchRequest;
import com.example.CineBook.dto.ticket.TicketBatchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(BookingCreateRequest request);
    BookingResponse createDraftBooking(BookingDraftRequest request);
    BookingResponse addTicketsBatch(UUID bookingId, TicketBatchRequest request);
    BookingResponse addProductsBatch(UUID bookingId, BookingProductBatchRequest request);
    BookingSummaryResponse getBookingSummary(UUID bookingId);
    BookingResponse checkout(UUID bookingId, BookingCheckoutRequest request);
    BookingResponse getBookingById(UUID id);
    List<BookingResponse> getBookingsByUserId(UUID userId);
    void deleteBooking(UUID id);
    BookingResponse cancelBooking(UUID bookingId, String reason);
    PageResponse<MyBookingResponse> getMyBookings(Pageable pageable);
}
