package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Debug", description = "Debug APIs")
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Operation(summary = "Kiểm tra authorities của user hiện tại")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> result = new HashMap<>();
        result.put("username", auth.getName());
        result.put("principal", auth.getPrincipal().toString());
        result.put("authenticated", auth.isAuthenticated());
        
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        result.put("authorities", authorities);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
