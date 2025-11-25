package com.example.CineBook.common.security;

import com.example.CineBook.dto.auth.AuthorityProjection;
import com.example.CineBook.dto.sysUser.UserAuthoritiesDTO;
import com.example.CineBook.repository.irepository.SysPermissionRepository;
import com.example.CineBook.repository.irepository.SysRoleRepository;
import com.example.CineBook.repository.irepository.SysUserRepository;
import com.example.CineBook.repository.redis.BlackListedTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtTokenProvider jwtTokenProvider;
    private final BlackListedTokenRepository blackListedTokenRepository;
    private final SysPermissionRepository sysPermissionRepository;
    private final SysRoleRepository sysRoleRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final SysUserRepository sysUserRepository;

    /**
     * Decides whether this filter should be applied to the current request.
     *
     * @param request The request to check.
     * @return {@code true} if filter SHOULD NOT be applied, {@code false} if vice versa.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURL = request.getRequestURI();
        for (String path : SecurityConfig.PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(path, requestURL)) {
                logger.debug("Path '{}' matches PUBLIC_ENDPOINTS pattern '{}'. Skipping JWT filter.", requestURL, path);
                return true;
            }
        }
        logger.trace("Path '{}' does not match any PUBLIC_ENDPOINTS patterns. Applying JWT filter.", requestURL);
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {

        try {
            // Get jwt from request
            String jwt = getJwtFromRequest(request);
            // Get username from jwt
            String username = jwtTokenProvider.getUsernameFromJWT(jwt);

            // Only perform validation and authentication if there is a JWT and the SecurityContext is empty.
            if (StringUtils.hasText(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtTokenProvider.validateToken(jwt) && !blackListedTokenRepository.existsByAccessToken(jwt)) {
                    // Now, getUserIdFromJWT return a string UUID
                    String userIdStr = jwtTokenProvider.getUserIdFromJWT(jwt);
                    UUID userId = UUID.fromString(userIdStr);

                    // 1. Get raw data list
                    List<AuthorityProjection> rawData = sysUserRepository.findAllAuthoritiesByUserId(userId);

                    // 2. Map into DTO
                    UserAuthoritiesDTO auths = new UserAuthoritiesDTO();
                    for (AuthorityProjection row : rawData) {
                        // Thêm Role (Set sẽ tự loại bỏ trùng lặp)
                        if (row.getRoleCode() != null) {
                            auths.getRoles().add(row.getRoleCode());
                        }
                        // Thêm Permission (Check null vì do LEFT JOIN có thể null)
                        if (row.getPermissionCode() != null) {
                            auths.getPermissions().add(row.getPermissionCode());
                        }
                    }

                    // 3. Create list GrantedAuthority for Spring Security
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    // Add Roles (Thêm prefix ROLE_)
                    auths.getRoles().stream().map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode)).forEach(authorities::add);
                    // Add Permissions
                    auths.getPermissions().stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);

                    // Create CustomUserDetails instead of using String
                    Authentication authentication = getAuthentication(userId, username, authorities);

                    // Set details using AbstractAuthenticationToken for polymorphism
                    ((AbstractAuthenticationToken) authentication)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // Ghi log lỗi để dễ dàng debug
            logger.error("Could not set user authentication in security context", ex);
            // Bắt tất cả các exception liên quan đến token (Expired, Signature, etc.)
            // và ủy quyền cho EntryPoint để trả về response lỗi 401 nhất quán.
            // Điều này ngăn exception lọt ra ngoài và bị xử lý bởi các handler khác, gây ra ClassCastException.
            jwtAuthenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid Token", ex));
        }
    }

    private static Authentication getAuthentication(UUID userId, String username, List<SimpleGrantedAuthority> authorities) {
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                username,  // username từ JWT
                "",         // password (không cần vì đã xác thực bằng JWT)
                authorities
        );

        // Set principal is CustomUserDetails
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities);
        return authentication;
    }

    /**
     * Extracts the JWT token from the request header.
     *
     * @param request The request to extract the JWT from.
     * @return The JWT token or {@code null} if not found.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


/**
 * I. SimpleGrantedAuthority
 * 1. Đại diện một quyền của user trong Spring Security
 * 2. Được Spring sử dụng để check role & authority
 * 3. Là class phổ biến nhất khi mapping quyền từ DB vào UserDetails
 * 4. Bắt buộc phải dùng nếu bạn tự tạo UserDetails hoặc implement UserDetailsService
 * 5. Được dùng trong filter JWT để đóng gói quyền vào SecurityContext
 * <p>
 * II. Ưu tiên dùng Authentication hơn UsernamePasswordAuthenticationToken
 * Vì Spring Security làm việc gần như 100% thông qua interface Authentication
 * Class UsernamePasswordAuthenticationToken chỉ là một trong nhiều implement
 * Khi dùng Spring Security phức tạp hơn sẽ gặp các class implement Authentication
 * 1. UsernamePasswordAuthenticationToken: Đại diện cho việc xác thực bằng username và password
 * 2. JwtAuthenticationToken: Đại diện cho việc xác thực bằng JWT
 * 3. OAuth2AuthenticationToken: Đại diện cho việc xác thực bằng OAuth2
 * 4. PreAuthenticatedAuthenticationToken: Đại diện cho việc xác thực bằng thông tin đã được xác thực trước đó
 * 5. AnonymousAuthenticationToken: Đại diện cho việc xác thực bằng Anonymous
 * 6. RememberMeAuthenticationToken: Đại diện cho việc xác thực bằng RememberMe
 * 7. WebAuthenticationDetails: Đại diện cho thông tin chi tiết về yêu cầu HTTP
 * 8. WebAuthenticationDetailsSource: Đại diện cho nguồn thông tin chi tiết về yêu cầu HTTP
 * 9. AbstractAuthenticationToken: Đại diện cho một loại xác thực cụ thể
 * 10. Authentication: Đại diện cho một loại xác thực cụ thể
 * 11. AuthenticationManager: Đại diện cho quản lý xác thực
 * 12. AuthenticationProvider: Đại diện cho một cách xác thực cụ thể
 * 13. AuthenticationTrustResolver: Đại diện cho một cách xác thực cụ thể
 * 14. GrantedAuthority: Đại diện cho một quyền của user trong Spring Security
 * 15. SecurityContext: Đại diện cho một bộ lọc xác thực trong Spring Security
 * 16. SecurityContextHolder: Đại diện cho một bộ lọc xác thực trong Spring Security
 * 17. SecurityContextImpl: Đại diện cho một bộ lọc xác thực trong Spring Security
 * 18. SecurityContextRepository: Đại diện cho một bộ lọc xác thực trong Spring Security
 * 19. SecurityFilterChain: Đại diện cho một bộ lọc xác thực trong Spring Security
 * 20. SecurityMetadataSource: Đại diện cho một bộ lọc xác thực trong Spring Security
 * 21. SimpleGrantedAuthority: Đại diện cho một quyền của user trong Spring Security
 * 22. User: Đại diện cho một user trong Spring Security
 * 23. UserDetails: Đại diện cho một user trong Spring Security
 * 24. UserDetailsService: Đại diện cho một cách lấy thông tin user trong Spring Security
 * 25. WebAuthenticationDetailsSource: Đại diện cho nguồn thông tin chi tiết về yêu cầu HTTP
 * 26. WebAuthenticationDetailsSource: Đại diện cho nguồn thông tin chi tiết về yêu cầu HTTP
 * Nếu muốn code phụ thuộc vào UsernamePasswordAuthenticationToken --> Code sẽ cứng, khó tái sử dụng, khó mở rộng
 * Nếu dùng Authentication --> Sẽ dùng được tất cả các implementation trên
 *
 */
