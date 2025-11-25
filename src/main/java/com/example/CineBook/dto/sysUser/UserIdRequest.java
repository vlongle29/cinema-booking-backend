package com.example.CineBook.dto.sysUser;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserIdRequest {
    @NotEmpty(message = "{USER_ID_LIST_REQUIRED}")
    private List<UUID> ids;
}
