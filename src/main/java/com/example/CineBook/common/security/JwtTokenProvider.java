package com.example.CineBook.common.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final TokenHasher tokenHasher;

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs: 86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo access token mới chứa sessionId.
     *
     * @param authentication Đối tượng xác thực
     * @param sessionId      ID của phiên đăng nhập
     * @return Chuỗi JWT
     */
    public String generateToken(Authentication authentication, UUID sessionId, String username) {
        String userId = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("sessionId", sessionId.toString()) // Thêm sessionId vào claims
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }

    /**
     * Lấy sessionId từ trong một token.
     * @param token Chuỗi JWT
     * @return UUID của session
     * @throws JwtException nếu claim 'sessionId' không tồn tại
     */
    public UUID getSessionIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.get("sessionId", String.class));
    }

    /**
     * Tạo refresh token mới chứa sessionId.
     * @param userId ID của người dùng
     * @param sessionId ID của phiên đăng nhập
     * @return Chuỗi JWT
     */
    public String generateRefreshToken(String userId, UUID sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 7L * 24 * 60 * 60 * 1000); // 30 ngày
        return Jwts.builder()
                .subject(userId)
                .claim("sessionId", sessionId.toString()) // Thêm sessionId vào claims
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key) // Use the SecretKey object
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public String hashToken(String token) {
        return tokenHasher.hashToken(token);
    }

    public String getUserIdFromRefreshToken(String token) {
        return getUserIdFromJWT(token);
    }
}


///  <============= Kiến thức học được ================>

/**
 * org.springframework.beans.factory.annotation.Value
 *
 * @Value: Là anotation của Spring Framework
 * -> Dùng để inject(gán) giá trị từ cấu hình (configuration) vào một field, method, hoặc constructor parameter
 * VD: @Value("${server.port}")   // lấy giá trị từ application.properties hoặc application.yml
 * private int serverPort;
 * @Value("Hello Spring") // gán giá trị trực tiếp
 * private String greeting;
 *
 * @PostConstruct:
 *
 */
