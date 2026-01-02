package com.example.CineBook.websocket;

import com.example.CineBook.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        try {
            // Get token from query parameter: ws://localhost:8080/ws/seats/{id}?token=xxx
            String query = request.getURI().getQuery();
            String token = UriComponentsBuilder.fromUriString("?" + query)
                    .build()
                    .getQueryParams()
                    .getFirst("token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket connection rejected: No token provided");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket connection rejected: Invalid token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            String userId = jwtTokenProvider.getUserIdFromJWT(token);
            attributes.put("userId", userId);

            log.info("WebSocket authenticated for user: {}", userId);
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
