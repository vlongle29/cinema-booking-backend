package com.example.CineBook.websocket.handle;

import com.example.CineBook.websocket.service.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Extract showtimeId from URL: /ws/seats/{showtimeId}
//        String path = session.getUri().getPath();
//        String id = path.substring(path.lastIndexOf('/') + 1);

        // Gửi welcome message
        try {
            var response = java.util.Map.of(
                    "type", "CONNECTED",
                    "sessionId", session.getId()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            log.error("Failed to send welcome message", e);
        }

        //        sessionManager.subscribe(UUID.fromString(id), session);
//        log.info("WebSocket connected: sessionId={}, showtimeId={}", session.getId(), showtimeId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("Received message from {}: {}", session.getId(), payload);

            // Parse JSON message
            var messageData = objectMapper.readValue(payload, java.util.Map.class);
            String type = (String) messageData.get("type");

            if ("SUBSCRIBE_SHOWTIME".equals(type)) {
                String showtimeId = (String) messageData.get("showtimeId");

                // Add session to showtime subscribers
                sessionManager.subscribe("showtime", UUID.fromString(showtimeId), session);

                // Send confirmation response
                var response = java.util.Map.of(
                        "type", "SUBSCRIBED",
                        "topic", "showtime",
                        "showtimeId", showtimeId
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            } else if ("SUBSCRIBE_BOOKING".equals(type)) {
                String bookingId = (String) messageData.get("bookingId");

                // Add session to booking subscribes
                sessionManager.subscribe("booking", UUID.fromString(bookingId), session);

                var response = java.util.Map.of(
                        "type", "SUBSCRIBED",
                        "topic", "booking",
                        "bookingId", bookingId
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            } else if ("PING".equals(type)) {
                // Heartbeat response
                var response = java.util.Map.of("type", "PONG");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            } else {
                log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling message from {}", session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.removeSession(session);
        log.info("WebSocket disconnected: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket error: sessionId={}", session.getId(), exception);
        sessionManager.removeSession(session);
    }
}
