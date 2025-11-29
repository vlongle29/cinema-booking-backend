package com.example.CineBook.dto.employee;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;

import java.util.UUID;

@Data
public class EmployeeSearchDTO extends SearchBaseDto {
    private String name;
    private String employeeCode;
    private UUID branchId;
    private String role;
}
