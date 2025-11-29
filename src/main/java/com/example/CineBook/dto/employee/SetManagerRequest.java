package com.example.CineBook.dto.employee;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SetManagerRequest {
    @NotNull(message = "{MANAGER_ID_REQUIRE}")
    private UUID managerId;
}
