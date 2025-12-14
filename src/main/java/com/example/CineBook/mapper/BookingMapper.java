package com.example.CineBook.mapper;

import com.example.CineBook.dto.booking.BookingCreateRequest;
import com.example.CineBook.dto.booking.BookingResponse;
import com.example.CineBook.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper extends BaseMapper<BookingCreateRequest, Booking> {
    Booking map(BookingCreateRequest request);
    BookingResponse toResponse(Booking entity);
}
