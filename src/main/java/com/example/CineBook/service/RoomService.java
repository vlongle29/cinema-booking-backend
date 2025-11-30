package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.room.RoomRequest;
import com.example.CineBook.dto.room.RoomResponse;
import com.example.CineBook.dto.room.RoomSearchDTO;

import java.util.UUID;

public interface RoomService {
    RoomResponse createRoom(RoomRequest request);
    RoomResponse getRoomById(UUID id);
    RoomResponse updateRoom(UUID id, RoomRequest request);
    void deleteRoom(UUID id);
    void deleteRoomCascade(UUID id);
    PageResponse<RoomResponse> searchRooms(RoomSearchDTO searchDTO);
    List<RoomResponse> getRoomsByBranch(UUID branchId);
}
