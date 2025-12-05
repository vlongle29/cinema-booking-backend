package com.example.CineBook.common.exception;

import lombok.Getter;

@Getter
public enum MessageCode {
    // Category
    CATEGORY_NOT_FOUND,
    CATEGORY_NAME_REQUIRED,
    DUPLICATE_MENU_NAME,

    // Role
    SYS_ROLE_NOT_FOUND,
    SYS_ROLE_CODE_ALREADY_EXISTS,
    SYS_ROLE_CANNOT_DELETE_SYSTEM_ROLE,
    SYS_ROLE_CANNOT_MODIFY_SYSTEM_ROLE_PERMISSIONS,
    ROLE_NAME_REQUIRED,
    ROLE_CODE_REQUIRED,
    ASSIGN_ROLE_USER_ID_REQUIRED,
    ASSIGN_ROLE_ROLE_IDS_REQUIRED,

    // Branch + ROOM + SEAT
    BRANCH_NOT_FOUND,
    BRANCH_ALREADY_EXISTS,
    BRANCH_HAS_ROOMS,
    ROOM_NOT_FOUND,
    ROOM_HAS_SEAT,
    ROOM_ALREADY_EXISTS,
    SEAT_ALREADY_EXISTS,

    // Genres, Movies, Movie-Genre
    GENRE_NAME_ALREADY_EXISTS,
    GENRE_NOT_FOUND,
    GENRE_IN_USE,
    MOVIE_NOT_FOUND,
    ROOM_HAS_SEATS,



    // Auth & User & Employee & STAFF & POSITION
    DELETE_FAIL("Xóa thất bại"),
    PHONE_ALREADY_EXISTS("Số điện thoại đã tồn tại"),
    LOGIN_FAIL("Đăng nhập thất bại"),
    ACCOUNT_LOCKED("Tài khoản đã bị khóa"),
    ACCOUNT_NOT_EXISTS("Tài khoản không tồn tại"),
    USER_NOT_FOUND("Người dùng không tồn tại"),
    EMAIL_ALREADY_EXISTS("Email đã tồn tại"),
    USERNAME_ALREADY_EXISTS("Tên đăng nhập đã tồn tại"),
    PASSWORD_CHANGE_FAILED("Đổi mật khẩu thất bại"),
    PASSWORD_RESET_FAILED("Mật khẩu và xác nhận mật khẩu không khớp"),
    CHANGE_PASSWORD_USER_ID_REQUIRED("ID người dùng là bắt buộc"),
    CHANGE_PASSWORD_OLD_REQUIRED("Mật khẩu cũ là bắt buộc"),
    CHANGE_PASSWORD_OLD_SIZE("Mật khẩu cũ không hợp lệ"),
    CHANGE_PASSWORD_NEW_REQUIRED("Mật khẩu mới là bắt buộc"),
    CHANGE_PASSWORD_NEW_SIZE("Mật khẩu mới không hợp lệ"),
    CHANGE_PASSWORD_NEW_PATTERN("Mật khẩu mới không đúng định dạng"),
    USERNAME_REQUIRED("Tên đăng nhập là bắt buộc"),
    PASSWORD_REQUIRED("Mật khẩu là bắt buộc"),
    PASSWORD_WRONG("Mật khẩu không đúng"),
    USER_USERNAME_REQUIRED("Tên đăng nhập là bắt buộc"),
    USER_PASSWORD_REQUIRED("Mật khẩu là bắt buộc"),
    USER_NAME_REQUIRED("Tên người dùng là bắt buộc"),
    USER_EMAIL_REQUIRED("Email là bắt buộc"),
    USER_PHONE_REQUIRED("Số điện thoại là bắt buộc"),
    USER_WITH_NO_ROLE("Người dùng chưa có vai trò"),
    SESSION_EXPIRED("Phiên đăng nhập đã hết hạn"),
    SESSION_NOT_FOUND("Phiên đăng nhập không tồn tại"),
    INVALID_TOKEN("Token không hợp lệ"),
    EMPLOYEE_CODE_ALREADY_EXISTS,
    STAFF_ROLE_NOT_CONFIGURED,
    EMPLOYEE_NOT_FOUND,
    INVALID_POSITION,
    POSITION_NOT_FOUND,
    POSITION_CODE_ALREADY_EXISTS,
    NOT_EMPLOYEE,
    INSUFFICIENT_POSITION,
    MANAGER_ALREADY_EXISTS,
    CANNOT_TRANSFER_MANAGER,

    // General Errors
    INTERNAL_SERVER_ERROR("Lỗi hệ thống"),
    BAD_REQUEST("Yêu cầu không hợp lệ"),
    UNAUTHORIZED("Chưa xác thực"),
    FORBIDDEN("Không có quyền truy cập"),
    VALIDATION_FAILED("Dữ liệu không hợp lệ"),

    // =================================================================
    // NOTE: Các mã dưới đây biểu thị trạng thái THÀNH CÔNG,
    // nên cân nhắc chuyển ra một Enum khác để tránh nhầm lẫn với mã LỖI.
    // =================================================================
    SUCCESS("Thành công"),
    ENTITY_NOT_FOUND("Không tìm thấy dữ liệu"),
    // Permission
    SYS_PERMISSION_NOT_FOUND,
    SYS_PERMISSION_ALREADY_EXISTS,
    SYS_PERMISSION_CANNOT_DELETE_SYSTEM_PERMISSION,
    SYS_PERMISSION_CANNOT_MODIFY_SYSTEM_PERMISSION,
    PERMISSION_NAME_REQUIRED,
    PERMISSION_NAME_MAX_SIZE,
    PERMISSION_DESCRIPTION_MAX_SIZE,

    // Permission related errors
    PERMISSION_UPDATE_OWN_ONLY,
    PERMISSION_DELETE_OWN_ONLY,
    PERMISSION_CANCEL_OWN_ONLY,
    PERMISSION_APPROVE_DENIED,
    PERMISSION_REJECT_DENIED,
    PERMISSION_VIEW_PENDING_DENIED,

    // Conflict related errors
    CONFLICTING_REQUESTS,

    // Not found errors
    USER_REQUEST_NOT_FOUND,
    REQUEST_PROCESS_NOT_FOUND,

    // Action related errors
    INVALID_STATE_NAME_IN_TRANSITION,
    INITIAL_STATE_NOT_FOUND,
    MULTIPLE_INITIAL_STATES_NOT_ALLOWED,
    DUPLICATE_STATE_NAME_IN_REQUEST,
    ACTION_NOT_ALLOWED,
    NOTE_REQUIRED,
    PERMISSION_DENIED,
    WORKFLOW_INITIAL_STATE_NOT_FOUND,
    WORKFLOW_INITIAL_STATE_MISMATCH,
    WORKFLOW_NOT_ACTIVE,
    PERMISSION_CANCEL_DENIED,
    INVALID_WORKFLOW_ACTION,
    STATUS_ONLY_PERFORM_ACTION_PENDING,
    PERMISSION_ACTION_DENIED,
    INVALID_METADATA_FORMAT,
    INVALID_WORKFLOW_ROLE,
    WORK_SCHEDULE_DETAIL_NOT_FOUND,
    WORK_SCHEDULE_IS_HOLIDAY,
    WORK_SCHEDULE_NOT_FOUND, REQUEST_CATEGORY_NOT_FOUND,
    REQUEST_CATEGORY_CODE_ALREADY_EXISTS,
    REQUEST_CATEGORY_NAME_ALREADY_EXISTS,
    BULK_ACTION_DIFFERENT_TYPES,
    BULK_ACTION_INVALID_IDS;

    private final String message;

    /**
     * Constructor cho các mã lỗi có thông điệp tường minh.
     *
     * @param message Thông điệp lỗi dành cho người dùng.
     */
    MessageCode(String message) {
        this.message = message;
    }

    /**
     * Constructor cho các mã trạng thái (thường là thành công),
     * không có thông điệp lỗi cụ thể.
     */
    MessageCode() {
        this.message = null;
    }
}
