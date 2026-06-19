package com.example.CineBook.common.util;

import org.springframework.http.ResponseCookie;

public class CookieUtils {

    private static final int COOKIE_MAX_AGE = 8 * 24 * 60 * 60; // 8 days

    public static ResponseCookie createCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAge)
                .build();

    }

    public static ResponseCookie clearCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAge)
                .build();

    }

}
