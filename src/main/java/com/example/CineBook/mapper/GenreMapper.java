package com.example.CineBook.mapper;

import com.example.CineBook.dto.genre.CreateGenreRequest;
import com.example.CineBook.dto.genre.GenreResponse;
import com.example.CineBook.dto.genre.UpdateGenreRequest;
import com.example.CineBook.model.Genre;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GenreMapper {
    
    Genre toEntity(CreateGenreRequest request);
    
    void updateEntityFromDto(UpdateGenreRequest request, @MappingTarget Genre genre);
    
    GenreResponse toResponse(Genre genre);
}
