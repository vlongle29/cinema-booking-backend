package com.example.CineBook.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleCodeRequestDTO {
    @NotBlank
    private String code;
}
