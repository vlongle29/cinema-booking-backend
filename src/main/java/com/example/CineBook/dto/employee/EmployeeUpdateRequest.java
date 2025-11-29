package com.example.CineBook.dto.employee;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeUpdateRequest {
    private BigDecimal salary;
    private LocalDate hireDate;
    private UUID branchId;
    private String position;
    private String name;
    private String email;
    private String phone;
}
