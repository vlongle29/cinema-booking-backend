package com.example.CineBook.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "{USERNAME_REQUIRED}")
    private String username;

    @NotBlank(message = "{PASSWORD_REQUIRED}")
    private String password;

}
