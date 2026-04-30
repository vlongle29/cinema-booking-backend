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
    @Mapping(target = "rowChar", qualifiedByName = "extractRowChar", source = "request.seatNumber")
    @Mapping(target = "rowIndex", qualifiedByName = "extractRowIndex", source = "request.seatNumber")
    @Mapping(target = "columnIndex", qualifiedByName = "extractColumnIndex", source = "request.seatNumber")
    @Mapping(target = "seatNum", qualifiedByName = "extractSeatNumber", source = "request.seatNumber")
    @Mapping(target = "seatTypeId", source = "request.seatTypeId")
    @Mapping(target = "isAisle", source = "request.isAisle")
    SeatTemplateDetail toDetailEntity(SeatTemplateDetailRequest request, UUID templateId);

    @Mapping(target = "id", source = "detail.id")
    @Mapping(target = "seatNumber", expression = "java(combineSeatNumber(detail))")
    @Mapping(target = "rowChar", source = "detail.rowChar")
    @Mapping(target = "rowIndex", source = "detail.rowIndex")
    @Mapping(target = "columnIndex", source = "detail.columnIndex")
    @Mapping(target = "seatNum", source = "detail.seatNum")
    @Mapping(target = "seatTypeId", source = "detail.seatTypeId")
    @Mapping(target = "isAisle", source = "detail.isAisle")
    @Mapping(target = "seatType", source = "seatType")
    SeatTemplateDetailResponse toDetailResponse(SeatTemplateDetail detail, SeatType seatType);

    default String combineSeatNumber(SeatTemplateDetail detail) {
        return detail.getRowChar() + detail.getSeatNum();
    }

    @Named("extractRowChar")
    default String extractRowChar(String seatNumber) {
        return seatNumber.replaceAll("[0-9]", "");
    }

    @Named("extractRowIndex")
    default Integer extractRowIndex(String seatNumber) {
        // Convert A->0, B->1, C->2, etc.
        String rowChar = seatNumber.replaceAll("[0-9]", "");
        return rowChar.charAt(0) - 'A';
    }

    @Named("extractColumnIndex")
    default Integer extractColumnIndex(String seatNumber) {
        // Extract number and convert to 0-based index
        Integer seatNum = Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
        return seatNum - 1;
    }

    @Named("extractSeatNumber")
    default Integer extractSeatNumber(String seatNumber) {
        return Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
    }
}
