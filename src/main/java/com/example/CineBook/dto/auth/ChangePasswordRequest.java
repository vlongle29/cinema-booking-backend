package com.example.CineBook.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotNull(message = "{CHANGE_PASSWORD_USER_ID_REQUIRED}")
    private UUID userId;
    @NotBlank(message = "{CHANGE_PASSWORD_OLD_REQUIRED}")
    @Size(min = 6, max = 50, message = "{CHANGE_PASSWORD_OLD_SIZE}")
    private String oldPassword;
    @NotBlank(message = "{CHANGE_PASSWORD_NEW_REQUIRED}")
    @Size(min = 6, max = 50, message = "{CHANGE_PASSWORD_NEW_SIZE}")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
            message = "{CHANGE_PASSWORD_NEW_PATTERN}"
    )
    private String newPassword;
}
