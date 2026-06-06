package com.example.CineBook.mapper;

import com.example.CineBook.dto.review.CreateReviewRequest;
import com.example.CineBook.dto.review.ReviewResponse;
import com.example.CineBook.model.Review;
import com.example.CineBook.repository.irepository.ReviewRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    Review toEntity(CreateReviewRequest request);
    
    @Mapping(target = "createdAt", source = "createTime")
    @Mapping(target = "updatedAt", source = "updateTime")
    ReviewResponse toResponse(Review review);
}
