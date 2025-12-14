package com.example.CineBook.mapper;

import com.example.CineBook.dto.booking.BookingCreateRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductCreateRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductResponse;
import com.example.CineBook.model.BookingProduct;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingProductMapper extends BaseMapper<BookingProductCreateRequest, BookingProduct> {
    BookingProduct map(BookingCreateRequest request);
    BookingProductResponse toResponse(BookingProduct entity);
}
