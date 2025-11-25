package com.example.CineBook.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ResetPasswordRequest {
    @NotNull(message = "{RESET_PASSWORD_USER_ID_REQUIRED}")
    private UUID userId;
    @NotBlank(message = "{RESET_PASSWORD_NEW_PASSWORD_REQUIRED}")
    private String newPassword;
}
