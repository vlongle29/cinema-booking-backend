package com.example.CineBook.dto.room;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class RoomSearchDTO extends SearchBaseDto {
    private String name;
    private UUID branchId;
}
