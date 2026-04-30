package com.example.CineBook.websocket;

import com.example.CineBook.common.security.SessionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        try {
            List<String> cookieHeaders = request.getHeaders().get("Cookie");

            if (cookieHeaders == null || cookieHeaders.isEmpty()) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            String cookieHeader = cookieHeaders.get(0);

            Map<String, String> cookiesMap = new HashMap<>();
            for (String cookie : cookieHeader.split(";")) {
                cookie = cookie.trim();
                String[] parts = cookie.split("=", 2);
                if (parts.length == 2) {
                    cookiesMap.put(parts[0], parts[1]);
                }
            }

            String sessionId = cookiesMap.get("sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                log.warn("WebSocket connection rejected: No sessionId cookie");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            UUID sessionUuid;
            try {
                sessionUuid = UUID.fromString(sessionId);
            } catch (IllegalArgumentException e) {
                log.warn("WebSocket connection rejected: Invalid sessionId format");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionUuid.toString());
            if (sessionInfo == null) {
                log.warn("WebSocket connection rejected: Session not found");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            if (sessionInfo.expiry().before(new Date())) {
                log.warn("WebSocket connection rejected: Session expired");
                redisTemplate.delete(sessionUuid.toString());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("userId", sessionInfo.userId().toString());
            attributes.put("username", sessionInfo.username());
            log.info("WebSocket authenticated for user: {} ({})", sessionInfo.username(), sessionInfo.userId());
            return true;
        } catch (Exception e) {
            log.error("WebSocket authentication error", e);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Do nothing
    }
}
