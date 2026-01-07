package com.example.CineBook.mapper;

import com.example.CineBook.dto.showtime.CreateShowtimeRequest;
import com.example.CineBook.dto.showtime.ShowtimeResponse;
import com.example.CineBook.dto.showtime.UpdateShowtimeRequest;
import com.example.CineBook.model.Showtime;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShowtimeMapper {
    
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "basePrice", source = "price")
    Showtime toEntity(CreateShowtimeRequest request);
    
    @Mapping(target = "basePrice", source = "price")
    void updateEntityFromDto(UpdateShowtimeRequest request, @MappingTarget Showtime showtime);
    
    @Mapping(target = "price", source = "basePrice")
    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "branch", ignore = true)
    ShowtimeResponse toResponse(Showtime showtime);
}
