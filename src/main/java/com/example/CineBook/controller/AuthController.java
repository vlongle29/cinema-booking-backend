package com.example.CineBook.controller;

import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.common.util.CookieUtils;
import com.example.CineBook.common.util.RequestUtils;
import com.example.CineBook.dto.auth.*;
import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.example.CineBook.service.AuthService;
import com.example.CineBook.service.GoogleOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Authentication", description = "Các API để xác thực người dùng")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Operation(summary = "Đăng ký", description = "Tạo tài khoản mới.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @Operation(summary = "Đăng nhập", description = "Xác thực người dùng và trả về token.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String ipAddress = RequestUtils.getClientIp(httpRequest);
        String device = RequestUtils.getDeviceInfo(httpRequest);

        LoginResponse loginResponse = authService.login(request, device, ipAddress);

        // Set sessionId cookie (session locator)
        // Nếu cấu hình như này để lưu sessionId vào cookie httpOnly, vì thế fe nhận được sẽ tự động lưu sessionId vào cookie
        // Còn nếu gửi sessionId thông thường về cho fe thì fe sẽ phải tự động lưu sessionId vào cookie --> Thiếu an toàn hơn vì js có thể can thiệp
        // fe chỉ cần gửi --> axios.post("/auth/login", data, { withCredentials: true });
        ResponseCookie sessionCookie = ResponseCookie.from("sessionId", loginResponse.getSessionId().toString())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(8 * 24 * 60 * 60) // 8 days
                .build();

        // Set refreshToken cookie (credential)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(8 * 24 * 60 * 60) // 8 days
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(loginResponse));
    }

    @Operation(summary = "Làm mới Access Token", description = "Lấy access token mới bằng refresh token hợp lệ.")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @CookieValue(value = "sessionId", required = false) UUID sessionId,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (sessionId == null || refreshToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(MessageCode.INVALID_TOKEN, 400));
        }
        
        LoginResponse loginResponse = authService.refreshToken(sessionId, refreshToken);
        
        // Rotate both cookies with new values
        ResponseCookie sessionCookie = ResponseCookie.from("sessionId", loginResponse.getSessionId().toString())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(8 * 24 * 60 * 60)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(8 * 24 * 60 * 60)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(loginResponse));
    }

    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa token hiện tại và xóa session.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(value = "sessionId", required = false) UUID sessionId) {
        
        authService.logout(authHeader, sessionId);
        
        // Clear cookies
        ResponseCookie clearSessionCookie = ResponseCookie.from("sessionId", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie clearRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearSessionCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                .body(ApiResponse.success());
    }

    // API 1: Frontend gọi để lấy link chuyển hướng sang Google
    @Operation(summary = "Lấy URL đăng nhập Google", description = "Trả về URL để chuyển hướng người dùng đến trang đăng nhập của Google.")
    @GetMapping("/google/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGoogleLoginUrl() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=email%20profile";
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url)));
    }

    // API 2: Frontend bắt được code từ URL và ném xuống đây
    @PostMapping("/google/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> handleGoogleCallback(@RequestBody @Valid GoogleCodeRequestDTO request,
                                                              @CookieValue(value = "sessionId", required = false) UUID sessionId, HttpServletRequest httpRequest) {
        String ipAddress = RequestUtils.getClientIp(httpRequest);
        String device = RequestUtils.getDeviceInfo(httpRequest);

        LoginResponse loginResponse = googleOAuthService.authenticateAndFetchProfile(request.getCode(), device, ipAddress);

        ResponseCookie sessionCookie = ResponseCookie.from("sessionId", loginResponse.getSessionId().toString())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(8 * 24 * 60 * 60) // 8 days
                .build();

        // Set refreshToken cookie (credential)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(8 * 24 * 60 * 60) // 8 days
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(loginResponse));
    }

    @Operation(summary = "Lấy thông tin người dùng hiện tại", description = "Lấy thông tin chi tiết của người dùng đã xác thực.")
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        UserInfoResponse userInfo = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success( userInfo));
    }

}


/**
 * httpOnlye = true
 * -> Cookie chỉ được gửi qua HTTP, không được gửi qua JavaScript.
 * -> Cookie được gửi từ server và không thể bị thay đổi bởi client-side JavaScript.
 * -> Ngăn XSS đánh cắp sessionId.
 *
 * secure = true
 * -> Cookie chỉ được gửi qua HTTPS, không gửi khi dùng HTTP.
 *
 * sameSite = Strict | Lax | None
 * - Strict: An toàn nhất → cookie chỉ gửi khi cùng domain
 * - Lax: An toàn + vẫn hoạt động với redirect
 * - None: Dùng cho SPA FE–BE khác domain → bắt buộc kèm secure=true
 *
 * Kết luận: Như phần comment ở trên khi cấu hình với cookie tôi đã làm ở trên
 */
