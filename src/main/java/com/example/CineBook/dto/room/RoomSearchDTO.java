package com.example.CineBook.dto.room;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;

import java.util.UUID;

@Data
public class RoomSearchDTO extends SearchBaseDto {
    private String name;
    private UUID branchId;
}
