package com.example.CineBook.mapper;

import com.example.CineBook.dto.seat.SeatRequest;
import com.example.CineBook.dto.seat.SeatResponse;
import com.example.CineBook.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    
    @Mapping(target = "seatNumber", expression = "java(combineSeatNumber(seat))")
    SeatResponse toResponse(Seat seat);
    
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
