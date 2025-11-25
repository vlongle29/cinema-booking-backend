package com.example.CineBook.common.security;

import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý các lỗi xác thực cho các request đến endpoint được bảo vệ mà không có token hợp lệ.
 * Component này sẽ trả về một response 401 Unauthorized chuẩn JSON.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Log lỗi để debug
        logger.error("Unauthorized error: {}. Path: {}", authException.getMessage(), request.getRequestURI());

        // Thiết lập response
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 4. Tạo response với message đã được lấy và status code chính xác
        ApiResponse<Object> apiResponse = ApiResponse.fail(
                MessageCode.UNAUTHORIZED,
                null,
                HttpServletResponse.SC_UNAUTHORIZED
        );

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
