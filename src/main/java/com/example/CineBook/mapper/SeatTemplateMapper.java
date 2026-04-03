package com.example.CineBook.mapper;

import com.example.CineBook.dto.seattemplate.*;
import com.example.CineBook.model.SeatTemplate;
import com.example.CineBook.model.SeatTemplateDetail;
import com.example.CineBook.model.SeatType;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatTemplateMapper {

    @Mapping(target = "totalSeats", constant = "0")
    SeatTemplate toEntity(CreateSeatTemplateRequest request);

    SeatTemplateResponse toResponse(SeatTemplate template);

    @Mapping(target = "templateId", source = "templateId")
    @Mapping(target = "rowChar", expression = "java(extractRowChar(request.getSeatNumber()))")
    @Mapping(target = "seatNum", expression = "java(extractSeatNumber(request.getSeatNumber()))")
    @Mapping(target = "seatTypeId", source = "request.seatTypeId")
    @Mapping(target = "isAisle", source = "request.isAisle")
    SeatTemplateDetail toDetailEntity(SeatTemplateDetailRequest request, UUID templateId);

    @Mapping(target = "seatNumber", expression = "java(combineSeatNumber(detail))")
    @Mapping(target = "seatType", source = "seatType")
    SeatTemplateDetailResponse toDetailResponse(SeatTemplateDetail detail, SeatType seatType);

    default String combineSeatNumber(SeatTemplateDetail detail) {
        return detail.getRowChar() + detail.getSeatNum();
    }

    default String extractRowChar(String seatNumber) {
        return seatNumber.replaceAll("[0-9]", "");
    }

    default Integer extractSeatNumber(String seatNumber) {
        return Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
    }
}
