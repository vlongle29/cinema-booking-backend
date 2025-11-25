package com.example.CineBook.common.response;

import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.util.MessageUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.CineBook.common.util.StaticContextAccessor;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final String code;
    private final int status;
    private final T data;
    private final List<Object> errors;

    private ApiResponse(boolean success, String message, String code, T data, int status, List<Object> errors) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.status = status;
        this.data = data;
        this.errors = errors;
    }

    /**
     * Helper nội bộ để lấy message đã được dịch từ MessageUtil.
     *
     * @param messageCode Enum MessageCode.
     * @param args        Các tham số cho message (nếu có).
     * @return Chuỗi message đã được dịch.
     */
    private static String getI18nMessage(MessageCode messageCode, Object... args) {
        try {
            // Sử dụng cầu nối để lấy bean MessageUtil và gọi phương thức getMessage
            MessageUtil messageUtil = StaticContextAccessor.getBean(MessageUtil.class);
            return messageUtil.getMessage(messageCode, args);
        } catch (Exception e) {
            // Fallback an toàn nếu có lỗi xảy ra (ví dụ: context chưa sẵn sàng)
            return messageCode.name();
        }
    }

    // =================================================================
    // SUCCESS RESPONSES - Đã cập nhật để dùng i18n
    // =================================================================

    /**
     * Trả về response thành công với message i18n mặc định và không có data.
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, getI18nMessage(MessageCode.SUCCESS), MessageCode.SUCCESS.name(), null, HttpStatus.OK.value(), null);
    }

    /**
     * Trả về response thành công với dữ liệu và message i18n mặc định.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, getI18nMessage(MessageCode.SUCCESS), MessageCode.SUCCESS.name(), data, HttpStatus.OK.value(), null);
    }

    /**
     * Trả về response thành công với dữ liệu và message i18n từ một MessageCode cụ thể.
     */
    public static <T> ApiResponse<T> success(MessageCode messageCode, T data) {
        return new ApiResponse<>(true, getI18nMessage(messageCode), messageCode.name(), data, HttpStatus.OK.value(), null);
    }

    /**
     * Trả về response thành công với dữ liệu, status code tùy chỉnh và message i18n mặc định.
     */
    public static <T> ApiResponse<T> success(T data, int status) {
        return new ApiResponse<>(true, getI18nMessage(MessageCode.SUCCESS), MessageCode.SUCCESS.name(), data, status, null);
    }

    // Các phương thức cho phép truyền message tùy chỉnh (giữ nguyên để linh hoạt)
    public static <T> ApiResponse<T> success(String customMessage, T data) {
        return new ApiResponse<>(true, customMessage, MessageCode.SUCCESS.name(), data, HttpStatus.OK.value(), null);
    }

    // =================================================================
    // FAIL RESPONSES - Đã cập nhật để dùng i18n
    // =================================================================

    /**
     * Trả về response thất bại với message i18n từ MessageCode.
     */
    public static <T> ApiResponse<T> fail(MessageCode messageCode) {
        return new ApiResponse<>(false, getI18nMessage(messageCode), messageCode.name(), null, HttpStatus.BAD_REQUEST.value(), null);
    }

    /**
     * Trả về response thất bại với message i18n, status code tùy chỉnh.
     */
    public static <T> ApiResponse<T> fail(MessageCode messageCode, int status) {
        return new ApiResponse<>(false, getI18nMessage(messageCode), messageCode.name(), null, status, null);
    }

    /**
     * Trả về response thất bại với message i18n, dữ liệu lỗi và status code tùy chỉnh.
     */
    public static <T> ApiResponse<T> fail(MessageCode messageCode, T data, int status) {
        return new ApiResponse<>(false, getI18nMessage(messageCode), messageCode.name(), data, status, null);
    }

    /**
     * Trả về response thất bại với message i18n có tham số, status code tùy chỉnh.
     */
    public static <T> ApiResponse<T> fail(MessageCode messageCode, int status, Object... args) {
        return new ApiResponse<>(false, getI18nMessage(messageCode, args), messageCode.name(), null, status, null);
    }

    /**
     * Trả về response thất bại với message tuỳ chỉnh (không qua MessageCode).
     */
    public static <T> ApiResponse<T> fail(String customMessage, T data, List<Object> errors) {
        return new ApiResponse<>(false, customMessage, MessageCode.VALIDATION_FAILED.name(), data, HttpStatus.BAD_REQUEST.value(), errors);
    }
}
