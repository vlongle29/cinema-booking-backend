package com.example.CineBook.dto.employee;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmployeeSearchDTO extends SearchBaseDto {
    private String name;
    private String employeeCode;
    private UUID branchId;
    private String role;
}
