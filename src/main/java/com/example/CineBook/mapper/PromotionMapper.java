package com.example.CineBook.mapper;

import com.example.CineBook.dto.promotion.PromotionRequest;
import com.example.CineBook.dto.promotion.PromotionResponse;
import com.example.CineBook.model.Promotion;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {
    
    Promotion toEntity(PromotionRequest request);
    
    void updateEntityFromDto(PromotionRequest request, @MappingTarget Promotion promotion);
    
    PromotionResponse toResponse(Promotion promotion);
}
