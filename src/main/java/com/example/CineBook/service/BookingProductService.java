package com.example.CineBook.service;

import com.example.CineBook.dto.bookingproduct.BookingProductCreateRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductResponse;

import java.util.List;
import java.util.UUID;

public interface BookingProductService {
    BookingProductResponse createBookingProduct(BookingProductCreateRequest request);
    List<BookingProductResponse> getBookingProductsByBookingId(UUID bookingId);
    void deleteBookingProduct(UUID id);
}
