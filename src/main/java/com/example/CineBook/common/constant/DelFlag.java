package com.example.CineBook.common.constant;

/**
 * Định nghĩa trạng thái xóa của đối tượng trong hệ thống.
 * Sử dụng để đánh dấu đối tượng đã bị xóa mềm (soft delete) hay chưa.
 */
public enum DelFlag {
    /**
     * Đối tượng đang ở trạng thái bình thường (chưa bị xóa).
     */
    NORMAL(false),
    /**
     * Đối tượng đã bị xóa (soft delete).
     */
    DELETED(true);
    /**
     * Giá trị trạng thái xóa (true nếu đã xóa, false nếu chưa xóa).
     */
    private final boolean value;
    /**
     * Constructor để gán giá trị cho trạng thái xóa.
     * @param value Giá trị trạng thái.
     */
    DelFlag(boolean value) {
        this.value = value;
    }
    /**
     * Lấy giá trị trạng thái xóa.
     * @return Giá trị trạng thái.
     */
    public boolean getValue() {
        return value;
    }
}
