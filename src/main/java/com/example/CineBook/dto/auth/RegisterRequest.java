package com.example.CineBook.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "{USER_USERNAME_REQUIRED}")
    private String username;

    @NotBlank(message = "{USER_PASSWORD_REQUIRED}")
    private String password;

    @NotBlank(message = "{USER_CONFIRM_PASSWORD_REQUIRED}")
    private String confirmPassword;

    @NotBlank(message = "{USER_NAME_REQUIRED}")
    private String name;

    @NotBlank(message = "{USER_EMAIL_REQUIRED}")
    private String email;

    @NotBlank(message = "{USER_PHONE_REQUIRED}")
    private String phone;
}
