package com.example.CineBook.dto.sysUser;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private UserInfoResponse userInfo;
    private List<String> permissions;
}
