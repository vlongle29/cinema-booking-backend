package com.example.CineBook.dto.sysUser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = "{USER_USERNAME_REQUIRED}")
    private String username;
    @NotBlank(message = "{USER_PASSWORD_REQUIRED}")
    private String password;
    @NotBlank(message = "{USER_NAME_REQUIRED}")
    private String name;
    @NotBlank(message = "{USER_EMAIL_REQUIRED}")
    private String email;
    @NotBlank(message = "{USER_PHONE_REQUIRED}")
    private String phone;
    private String avatar;
    private String systemFlag;
    @NotEmpty(message = "{ROLES_REQUIRED}")
    private List<UUID> roleIds;
}
