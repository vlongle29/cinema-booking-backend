package com.example.CineBook.mapper;

import com.example.CineBook.dto.room.RoomRequest;
import com.example.CineBook.dto.room.RoomResponse;
import com.example.CineBook.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomMapper extends BaseMapper<RoomRequest, Room> {
    
    @Mapping(target = "totalSeats", source = "capacity")
    RoomResponse toResponse(Room room);
    
    @Mapping(target = "capacity", source = "totalSeats")
    Room toEntity(RoomRequest request);
    
    @Mapping(target = "capacity", source = "totalSeats")
    void updateEntity(RoomRequest request, @MappingTarget Room room);
}
