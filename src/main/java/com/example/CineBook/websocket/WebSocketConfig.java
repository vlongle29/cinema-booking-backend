package com.example.CineBook.websocket;

import com.example.CineBook.websocket.handle.BookingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final BookingWebSocketHandler bookingWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(bookingWebSocketHandler, "/ws/booking")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*"); // Production: chỉ định domain cụ thể;
    }
}
