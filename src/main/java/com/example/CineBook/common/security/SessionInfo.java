package com.example.CineBook.common.security;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public record SessionInfo(UUID userId, String username, String refreshToken, Date expiry, String device, String ipAddress) implements Serializable {
}


///  <============== KIẾN THỨC HỌC ĐƯỢC ===============>

/**
 * public record SessionInfo
 * --> record tự động tạo:
 *     - Constructor
 *     - Getter
 *     - equals(), hashCode(), toString()
 *     và mặc định là immutable
 */


