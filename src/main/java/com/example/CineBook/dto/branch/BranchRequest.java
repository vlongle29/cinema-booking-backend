package com.example.CineBook.dto.branch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {
    
    @NotBlank(message = "Tên chi nhánh không được để trống")
    private String name;
    
    private String address;
    
    private String city;
    
    private UUID managerId;
}
