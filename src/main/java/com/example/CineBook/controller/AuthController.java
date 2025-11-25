package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.common.util.RequestUtils;
import com.example.CineBook.dto.auth.LoginRequest;
import com.example.CineBook.dto.auth.LoginResponse;
import com.example.CineBook.dto.auth.RegisterRequest;
import com.example.CineBook.dto.auth.RegisterResponse;
import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.example.CineBook.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Authentication", description = "Các API để xác thực người dùng")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

        // Nếu cấu hình như này để lưu sessionId vào cookie httpOnly, vì thế fe nhận được sẽ tự động lưu sessionId vào cokie
        // Còn nếu gửi sessionId thông thường về cho fe thì fe sẽ phải tự động lưu sessionId vào cookie --> Thiếu an toàn hơn vì js có thể can thiệp
        // fe chỉ cần gửi --> axios.post("/auth/login", data, { withCredentials: true });
        ResponseCookie cookie = ResponseCookie.from("sessionId", loginResponse.getSessionId().toString())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponse.success(loginResponse));
    }

    @Operation(summary = "Làm mới Access Token", description = "Lấy access token mới bằng refresh token hợp lệ.")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@CookieValue("sessionId") UUID sessionId) {
        LoginResponse loginResponse = authService.refreshToken(sessionId);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa token hiện tại.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader, @RequestBody UUID sessionId) {
        authService.logout(authHeader, sessionId);
        return ResponseEntity.ok(ApiResponse.success());
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
