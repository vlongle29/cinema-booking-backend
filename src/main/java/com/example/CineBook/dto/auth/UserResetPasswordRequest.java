package com.example.CineBook.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResetPasswordRequest {
    @NotNull(message = "{USER_EMAIL_REQUIRED}")
    private String email;
}
