package com.example.CineBook.dto.employee;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class EmployeeResponse {
    private UUID userId;
    private String username;
    private String name;
    private String email;
    private String phone;
    private UUID branchId;
    private String branchName;
    private String employeeCode;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private String role;
}
