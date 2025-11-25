package com.example.CineBook.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;
import java.util.UUID;

@RedisHash(value = "blacklisted_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {
    @Id
    private String accessToken;

    private Instant expiresAt;

    private UUID sessionId;

    @TimeToLive
    private Long ttl;

    // Sẽ được lưu trong Redis với key: "blacklisted_token:id" nhờ anotation @RedisHash
}


/**
 *
 * @RedisHash(value = "blacklisted_token")
 * Mục đích: Đánh dấu một class là redis entity
 *
 * Chức năng:
 * Chỉ định tên key prefix trong Redis (ở đây là "blacklisted_token")
 * Cho phép Spring Data Redis tự động serialize/deserialize object
 * Tạo mapping giữa Java object và Redis hash structure
 *
 */
