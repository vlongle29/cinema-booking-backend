package com.example.CineBook.common.security;

import com.example.CineBook.common.constant.PositionEnum;

import java.lang.annotation.*;

/**
 * Annotation để kiểm tra position của Employee.
 * Sử dụng kết hợp với @PreAuthorize để kiểm tra cả Role và Position.
 * 
 * Ví dụ:
 * @PreAuthorize("hasRole('STAFF')")
 * @RequirePosition({PositionEnum.MANAGER})
 * public void managerOnlyMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePosition {
    /**
     * Danh sách các position được phép truy cập.
     */
    PositionEnum[] value();
    
    /**
     * Thông báo lỗi khi không đủ quyền.
     */
    String message() default "Access denied: insufficient position";
}
