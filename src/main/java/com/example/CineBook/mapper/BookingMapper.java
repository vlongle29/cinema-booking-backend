package com.example.CineBook.mapper;

import com.example.CineBook.dto.booking.BookingCreateRequest;
import com.example.CineBook.dto.booking.BookingResponse;
import com.example.CineBook.dto.booking.MyBookingResponse;
import com.example.CineBook.model.*;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper extends BaseMapper<BookingCreateRequest, Booking> {
    Booking map(BookingCreateRequest request);
    BookingResponse toResponse(Booking entity);

    @Mapping(target = "id", source = "booking.id")
    @Mapping(target = "showtimeId", source = "booking.showtimeId")
    @Mapping(target = "showtimeStartTime", source = "showtime.startTime")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "moviePosterUrl", source = "movie.posterUrl")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "ticketCount", source = "ticketCount")
    @Mapping(target = "totalTicketPrice", source = "booking.totalTicketPrice")
    @Mapping(target = "totalFoodPrice", source = "booking.totalFoodPrice")
    @Mapping(target = "discountAmount", source = "booking.discountAmount")
    @Mapping(target = "finalAmount", source = "booking.finalAmount")
    @Mapping(target = "bookingDate", source = "booking.bookingDate")
    @Mapping(target = "status", source = "booking.status")
    @Mapping(target = "paymentMethod", source = "booking.paymentMethod")
    MyBookingResponse toMyBookingResponse(Booking booking, Showtime showtime, Movie movie,
                                          Branch branch, Room room, City city, int ticketCount);

}
