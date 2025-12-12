package com.example.CineBook.dto.branch;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class BranchSearchDTO extends SearchBaseDto {
    private String name;
    private String cityId;
    private UUID managerId;
}
