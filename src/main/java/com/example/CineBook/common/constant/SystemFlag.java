package com.example.CineBook.common.constant;

/**
 * Định nghĩa trạng thái hệ thống của đối tượng.
 * Sử dụng để phân biệt đối tượng hệ thống và đối tượng thông thường.
 */
public enum SystemFlag {
    /**
     * Đối tượng thông thường.
     */
    NORMAL("0"),

    /**
     * Đối tượng hệ thống.
     */
    SYSTEM("1"),

    /**
     * Đối tượng tài khoản SSO.
     */
    TYPE_ACCOUNT_SSO("SSO");

    /**
     * Giá trị trạng thái hệ thống.
     */
    private final String value;

    /**
     * Constructor để gán giá trị cho trạng thái hệ thống.
     * @param value Giá trị trạng thái.
     */
    SystemFlag(String value) {
        this.value = value;
    }

    /**
     * Lấy giá trị trạng thái hệ thống.
     * @return Giá trị trạng thái.
     */
    public String getValue() {
        return value;
    }
}
