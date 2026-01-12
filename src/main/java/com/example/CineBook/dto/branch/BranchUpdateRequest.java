package com.example.CineBook.dto.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchUpdateRequest {
    
    private String name;
    private String address;
    private UUID cityId;
    private UUID managerId;
}
