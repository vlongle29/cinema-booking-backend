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
    @Mapping(target = "type", ignore = true)
    Room toEntity(RoomRequest request);
    
    @Mapping(target = "capacity", source = "totalSeats")
    @Mapping(target = "type", ignore = true)
    void updateEntity(RoomRequest request, @MappingTarget Room room);
    
    @Override
    @Mapping(target = "capacity", source = "totalSeats")
    @Mapping(target = "type", ignore = true)
    Room map(RoomRequest source);
    
    @Override
    @Mapping(target = "capacity", source = "totalSeats")
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDelete", ignore = true)
    @Mapping(target = "deleteTime", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void update(RoomRequest source, @MappingTarget Room target);
}
