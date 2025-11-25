package com.example.CineBook.dto.auth;

import com.example.CineBook.dto.sysUser.UserInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private String accessToken;
    private UserInfoResponse userInfo;
    private List<String> permissions;
    private UUID sessionId;
//    private List<MenuResponse> menus;
}
