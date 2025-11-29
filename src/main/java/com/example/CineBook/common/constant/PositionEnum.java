package com.example.CineBook.common.constant;

import lombok.Getter;

/**
 * Enum định nghĩa các chức vụ nghiệp vụ của Employee.
 * Position khác với Role - Position là chức vụ công việc, Role là quyền hệ thống.
 */
@Getter
public enum PositionEnum {
    MANAGER("MANAGER", "Quản lý chi nhánh"),
    CASHIER("CASHIER", "Thu ngân"),
    TECHNICIAN("TECHNICIAN", "Kỹ thuật viên"),
    CLEANER("CLEANER", "Nhân viên vệ sinh"),
    SECURITY("SECURITY", "Bảo vệ");

    private final String value;
    private final String description;

    PositionEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static PositionEnum fromValue(String value) {
        for (PositionEnum position : PositionEnum.values()) {
            if (position.value.equals(value)) {
                return position;
            }
        }
        throw new IllegalArgumentException("Invalid position: " + value);
    }
}
