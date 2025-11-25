package com.example.CineBook.common.constant;

import lombok.Getter;

@Getter
public enum RoleEnum {
    SUPER_ADMIN("SUPER_ADMIN"),
    ADMIN("ADMIN"),
    STAFF("STAFF"),
    CUSTOMER("CUSTOMER");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

}
