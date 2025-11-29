package com.example.CineBook.dto.employee;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TransferEmployeeRequest {
    @NotNull(message = "{BRANCH_REQUIRED}")
    private UUID branchId;
}
