package com.example.CineBook.dto.sysUser;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class UserAuthoritiesDTO {
    // Dùng Set để không bị trùng Role hoặc Permission
    private Set<String> roles = new HashSet<>();
    private Set<String> permissions = new HashSet<>();
}
