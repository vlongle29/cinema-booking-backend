package com.example.CineBook.common.exception;

import com.example.CineBook.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private boolean isBinaryContentType(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase();
        return ct.contains("application/octet-stream")
                || ct.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || ct.contains("application/vnd.ms-excel");
    }

    /**
     * Nếu response đã có content-type là file/nhị phân:
     * - ưu tiên trả byte[] (tránh converter lỗi).
     * - nếu muốn trả JSON, cố gắng override content-type về application/json nếu response chưa commit.
     */
    private ResponseEntity<?> adaptResponseForCurrentContentType(Object body, HttpStatus status, HttpServletRequest request) {
        HttpServletResponse currentResponse = getCurrentResponse();

        if (currentResponse != null && isBinaryContentType(currentResponse.getContentType())) {
            return handleBinaryContentType(body, status, currentResponse);
        }

        return createJsonResponse(body, status);
    }

    private HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getResponse() : null;
    }

    private ResponseEntity<?> handleBinaryContentType(Object body, HttpStatus status, HttpServletResponse response) {
        if (body instanceof byte[]) {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body((byte[]) body);
        }

        if (response.isCommitted()) {
            return createEmptyBinaryResponse(status);
        }

        if (trySetJsonContentType(response)) {
            return createJsonResponse(body, status);
        }

        return createEmptyBinaryResponse(status);
    }

    private boolean trySetJsonContentType(HttpServletResponse response) {
        try {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            return true;
        } catch (Exception e) {
            log.warn("Không thể set content-type sang JSON: {}", e.getMessage());
            return false;
        }
    }

    private ResponseEntity<?> createEmptyBinaryResponse(HttpStatus status) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new byte[0]);
    }

    private ResponseEntity<?> createJsonResponse(Object body, HttpStatus status) {
        if (body instanceof ApiResponse) {
            return new ResponseEntity<>((ApiResponse<?>) body, status);
        }
        return new ResponseEntity<>(body, status);
    }

//    /**
//     * Check if field error valid
//     *
//     * @param ex
//     * @return
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ValidationErrorResponse> handleValidateException(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                "Validation failed",
//                "BAD_REQUEST",
//                errors
//        );
//
//        return ResponseEntity.badRequest().body(errorResponse);
//    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.fail(MessageCode.LOGIN_FAIL);
        return adaptResponseForCurrentContentType(response, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<?> handleAccountStatusException(AccountStatusException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.fail(MessageCode.ACCOUNT_LOCKED);
        return adaptResponseForCurrentContentType(response, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.fail(MessageCode.FORBIDDEN);
        return adaptResponseForCurrentContentType(response, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        MessageCode messageCode = ex.getMessageCode();
        HttpStatus status = switch (messageCode) {
            case SYS_ROLE_NOT_FOUND, USER_NOT_FOUND, CATEGORY_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case SYS_ROLE_CODE_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS, USERNAME_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case SYS_ROLE_CANNOT_DELETE_SYSTEM_ROLE, SYS_ROLE_CANNOT_MODIFY_SYSTEM_ROLE_PERMISSIONS, ACCOUNT_LOCKED,
                 FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return adaptResponseForCurrentContentType(ApiResponse.fail(messageCode), status, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Object> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());
        String messageKey = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        String code = messageKey != null && messageKey.startsWith("{") && messageKey.endsWith("}")
                ? messageKey.substring(1, messageKey.length() - 1)
                : null;
        if (code != null) {
            try {
                MessageCode messageCode = MessageCode.valueOf(code);
                return adaptResponseForCurrentContentType(ApiResponse.fail(messageCode), HttpStatus.BAD_REQUEST, request);
            } catch (Exception ignored) {
            }
        }
        return adaptResponseForCurrentContentType(ApiResponse.fail(messageKey, null, errors), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for request {} : {}", request != null ? request.getRequestURI() : "unknown", ex.getMessage(), ex);

        // Nếu muốn đặc biệt cho endpoint export, ta có thể trả byte[] rỗng.
        // Nhưng adaptResponseForCurrentContentType đã xử lý logic: nếu response là binary -> override content-type nếu có thể, hoặc trả byte[] rỗng nếu đã commit.
        MessageCode messageCode = MessageCode.INTERNAL_SERVER_ERROR;
        ApiResponse<Object> response = ApiResponse.fail(messageCode);
        return adaptResponseForCurrentContentType(response, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // Class cho lỗi validate field
    public record ValidationError(String field, String message) {
    }
}
