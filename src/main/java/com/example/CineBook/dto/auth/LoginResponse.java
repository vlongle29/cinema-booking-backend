package com.example.CineBook.dto.auth;

import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    // These fields are NOT serialized to JSON response
    // They are only used internally to set httpOnly cookies
    @JsonIgnore
    private UUID sessionId;
    
    @JsonIgnore
    private String refreshToken;
}
