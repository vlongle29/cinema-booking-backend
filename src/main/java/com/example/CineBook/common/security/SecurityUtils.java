package com.example.CineBook.common.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return Optional.ofNullable(((UserDetails) principal).getUsername());
        }
        return Optional.of(authentication.getName());
    }

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).userId();
        }

        // Fallback: try to parse from username if it's UUID format
        try {
            String username = (principal instanceof UserDetails)
                    ? ((UserDetails) principal).getUsername()
                    : authentication.getName();
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Cannot determine current user ID", e);
        }
    }

    /**
     * Kiểm tra user hiện tại có role cụ thể không
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role) || authority.equals(role));
    }

    /**
     * Kiểm tra user hiện tại có ít nhất một trong các roles không
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        Arrays.stream(roles).anyMatch(role ->
                                authority.equals("ROLE_" + role) || authority.equals(role)
                        )
                );
    }

    /**
     * Kiểm tra user có tất cả các roles không
     */
    public static boolean hasAllRoles(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return Arrays.stream(roles).allMatch(role ->
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority -> authority.equals("ROLE_" + role) || authority.equals(role))
        );
    }

    /**
     * Kiểm tra có authenticated không
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Lấy role đầu tiên (thường user chỉ có 1 role chính)
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)) // Bỏ prefix "ROLE_"
                .findFirst()
                .orElse(authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("USER")); // Default role nếu không tìm thấy
    }

    /**
     * Lấy tất cả roles của user hiện tại
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .collect(java.util.stream.Collectors.toList());
    }
}


/**
 * Trước khi đăng nhập:
 * -> auth instanceof AnonymousAuthenticationToken // true
 *
 * Sau khi đăng nhập thành công:
 * -> auth instanceof UsernamePasswordAuthenticationToken // true
 * -> auth instanceof AnonymousAuthenticationToken // false
 *
 * Note: Khi Authentication auth = new UserPasswordAuthenticationToken(....) thì lúc này auth đã rời khởi trạng thái AnonymousAuthenticationToken
 */
