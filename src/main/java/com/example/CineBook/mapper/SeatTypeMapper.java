package com.example.CineBook.mapper;

import com.example.CineBook.dto.seattype.SeatTypeRequest;
import com.example.CineBook.dto.seattype.SeatTypeResponse;
import com.example.CineBook.model.SeatType;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatTypeMapper {
    SeatType toEntity(SeatTypeRequest request);
    SeatTypeResponse toResponse(SeatType entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(SeatType entity, @MappingTarget SeatTypeRequest request);
}
