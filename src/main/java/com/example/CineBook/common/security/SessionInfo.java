package com.example.CineBook.common.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonTypeName("com.example.CineBook.common.security.SessionInfo")
public record SessionInfo(
        @JsonProperty("userId") UUID userId,
        @JsonProperty("username") String username,
        @JsonProperty("hashedRefreshToken") String hashedRefreshToken,
        @JsonProperty("expiry") Date expiry,
        @JsonProperty("device") String device,
        @JsonProperty("ipAddress") String ipAddress,
        @JsonProperty("createdAt") Date createdAt
) implements Serializable {
}


///  <============== KIẾN THỨC HỌC ĐƯỢC ===============>

/**
 * public record SessionInfo
 * --> record tự động tạo:
 *     - Constructor
 *     - Getter
 *     - equals(), hashCode(), toString()
 *     và mặc định là immutable
 *
 *     @JsonProperty: Tránh trường hợp fe gửi tên khác với tên thuộc tính
 *     kiểu nó quyết định fe gửi gì
 */


