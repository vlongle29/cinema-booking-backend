package com.example.CineBook.websocket.service;

import com.example.CineBook.websocket.dto.SeatStatusMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final ObjectMapper objectMapper;

    /**
     * Lưu trữ WebSocket sessions theo showtimeId
     *
     * Dùng ConcurrentHashMap thay vì HashMap vì:
     * 1. Thread-safe: Nhiều WebSocket connections có thể connect/disconnect đồng thời
     *    từ nhiều thread khác nhau (mỗi WebSocket connection chạy trên thread riêng)
     *
     * 2. Không cần synchronized: ConcurrentHashMap tự xử lý đồng bộ nội bộ,
     *    tránh phải wrap bằng Collections.synchronizedMap() hoặc synchronized block
     *
     * 3. Performance cao: Cho phép nhiều thread đọc/ghi đồng thời vào các segment khác nhau,
     *    còn synchronized HashMap chỉ cho 1 thread vào tại 1 thời điểm
     *
     * 4. Atomic operations: computeIfAbsent() được thực hiện atomic, đảm bảo không có
     *    race condition khi 2 thread cùng thêm session cho cùng 1 showtimeId (chen ngang)
     * VD: Thread A: containsKey → false
     *       ⏸️
     *     Thread B: containsKey → false
     *     Thread B: put(k, v2)
     *     Thread A: put(k, v1)   ← ghi đè
     *
     * 5. An toàn khi iterate: Không throw ConcurrentModificationException khi iterate
     *    trong khi có thread khác đang modify (như trong removeSession())
     *
     * Dùng CopyOnWriteArraySet vì:
     * - Thread-safe khi add/remove session
     * - Tối ưu cho trường hợp đọc nhiều, ghi ít (iterate trong broadcast() > add/remove)
     * - Không cần lock khi iterate
     */
    // Map: showtimeId -> Set of WebSocketSessions
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void subscribe(String topic, UUID id, WebSocketSession session) {
        String key = topic + ":" + id;
        sessions.computeIfAbsent(key, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("Session {} subscribed to {}", session.getId(), key);
    }

    public void unsubscribe(String topic, UUID id, WebSocketSession session) {
        String key = topic + ":" + id;
        CopyOnWriteArraySet<WebSocketSession> set = sessions.get(key);
        if (set != null) {
            sessions.remove(session);
            if (set.isEmpty()) {
                sessions.remove(key);
            }
        }
    }

    public void removeSession(WebSocketSession session) {
        sessions.values().forEach(set -> set.remove(session));
    }

    public void broadcast(String topic, UUID id, SeatStatusMessage message) {
        String key = topic + ":" + id;
        CopyOnWriteArraySet<WebSocketSession> set = sessions.get(key);
        if (set == null || set.isEmpty()) {
            log.debug("No active sessions for showtime {}", key);
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize message", e);
            return;
        }

        TextMessage textMessage = new TextMessage(payload);
        set.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                    log.debug("Sent message to session {}", session.getId());
                } catch (IOException e) {
                    log.error("Failed to send message to session {}", session.getId(), e);
                }
            }
        });
    }
}
