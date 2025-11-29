package com.example.CineBook.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeCreateRequest {
    @NotBlank(message = "{EMPLOYEE_USERNAME_REQUIRED}")
    private String username;

    @NotBlank(message = "{EMPLOYEE_PASSWORD_REQUIRED}")
    private String password;

    @NotBlank(message = "{EMPLOYEE_NAME_REQUIRED}")
    private String name;

    @NotBlank(message = "{EMPLOYEE_EMAIL_REQUIRED}")
    private String email;

    @NotBlank(message = "EMPLOYEE_PHONE_REQUIRED")
    private String phone;

    @NotNull(message = "EMPLOYEE_BRANCH_REQUIRED")
    private UUID branchId;

    @NotBlank(message = "{EMPLOYEE_CODE_REQUIRED}")
    private String employeeCode;

    @NotBlank(message = "{EMPLOYEE_POSITION_REQUIRED}")
    private String position;

    private BigDecimal salary;

    private LocalDate hireDate;
}
