package com.example.CineBook.service.impl;

import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.bookingproduct.BookingProductCreateRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductResponse;
import com.example.CineBook.mapper.BookingProductMapper;
import com.example.CineBook.model.BookingProduct;
import com.example.CineBook.repository.irepository.BookingProductRepository;
import com.example.CineBook.service.BookingProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingProductServiceImpl implements BookingProductService {

    private final BookingProductRepository bookingProductRepository;
    private final BookingProductMapper bookingProductMapper;

    @Override
    @Transactional
    public BookingProductResponse createBookingProduct(BookingProductCreateRequest request) {
        BookingProduct bookingProduct = bookingProductMapper.map(request);

        BookingProduct saved = bookingProductRepository.save(bookingProduct);
        return bookingProductMapper.toResponse(saved);
    }

    @Override
    public List<BookingProductResponse> getBookingProductsByBookingId(UUID bookingId) {
        return bookingProductRepository.findByBookingId(bookingId).stream()
                .map(bookingProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBookingProduct(UUID id) {
        if (!bookingProductRepository.existsById(id)) {
            throw new BusinessException(MessageCode.BOOKING_PRODUCT_NOT_FOUND);
        }
        bookingProductRepository.deleteById(id);
    }
}
