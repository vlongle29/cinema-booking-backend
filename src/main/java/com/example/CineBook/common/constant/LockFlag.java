package com.example.CineBook.common.constant;

import lombok.Getter;

/**
 * Định nghĩa trạng thái khóa của đối tượng.
 * Sử dụng để xác định đối tượng có bị khóa hay không.
 */
@Getter
public enum LockFlag {
    /**
     * Đối tượng ở trạng thái bình thường (không bị khóa).
     */
    NORMAL("0"),
    /**
     * Đối tượng đã bị khóa.
     */
    LOCK("9");

    /**
     * Giá trị trạng thái khóa.
     * -- GETTER --
     *  Lấy giá trị trạng thái khóa.
     */
    private final String value;

    /**
     * Constructor để gán giá trị cho trạng thái khóa.
     * @param value Giá trị trạng thái.
     */
    LockFlag(String value) {
        this.value = value;
    }

    /**
     * Chuyển giá trị từ DB (String) sang Enum.
     * @param value Giá trị lấy từ DB ("0", "9").
     * @return Enum LockFlag tương ứng.
     */
    public static LockFlag fromValue(String value) {
        for (LockFlag flag : LockFlag.values()) {
            if (flag.getValue().equals(value)) {
                return flag;
            }
        }
        throw new IllegalArgumentException("Invalid LockFlag value: " + value);
    }
}
