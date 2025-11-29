package com.example.CineBook.dto.branch;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import java.util.UUID;

@Data
public class BranchSearchDTO extends SearchBaseDto {
    private String name;
    private String city;
    private UUID managerId;
}
