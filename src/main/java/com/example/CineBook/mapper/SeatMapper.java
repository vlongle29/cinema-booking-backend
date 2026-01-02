package com.example.CineBook.mapper;

import com.example.CineBook.dto.seat.SeatRequest;
import com.example.CineBook.dto.seat.SeatResponse;
import com.example.CineBook.dto.seattype.SeatTypeResponse;
import com.example.CineBook.model.Seat;
import com.example.CineBook.model.SeatType;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatMapper {

    @Mapping(target = "id", source = "seat.id")
    @Mapping(target = "seatNumber", expression = "java(combineSeatNumber(seat))")
    @Mapping(target = "seatTypeId", source = "seat.seatTypeId")
    @Mapping(target = "roomId", source = "seat.roomId")
    @Mapping(target = "seatType", source = "seatType")
//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SeatResponse toResponse(Seat seat, SeatType seatType);

    @Mapping(target = "rowChar", expression = "java(extractRowChar(request.getSeatNumber()))")
    @Mapping(target = "seatNumber", expression = "java(extractSeatNumber(request.getSeatNumber()))")
    @Mapping(target = "roomId", source = "roomId")
    @Mapping(target = "status", ignore = true)
    Seat toEntity(SeatRequest request, UUID roomId);
    
    default String combineSeatNumber(Seat seat) {
        return seat.getRowChar() + seat.getSeatNumber();
    }
    
    default String extractRowChar(String seatNumber) {
        return seatNumber.replaceAll("[0-9]", "");
    }
    
    default Integer extractSeatNumber(String seatNumber) {
        return Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
    }
}
