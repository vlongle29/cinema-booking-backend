package com.example.CineBook.dto.sysUser;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UserUpdateRequest {
    private String username;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private String lockFlag;
    private String systemFlag;
    private String typeAccount;
//    private Instant birthday;
//    private String language;
    private List<UUID> roleIds;
}