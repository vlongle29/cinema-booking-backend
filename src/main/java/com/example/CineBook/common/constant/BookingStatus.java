package com.example.CineBook.common.constant;

public enum BookingStatus {
    DRAFT,              // Đang tạo (chưa thanh toán)
    PENDING_PAYMENT,    // Chờ thanh toán
    PAID,               // Đã thanh toán
    CONFIRMED,          // Đã xác nhận
    CANCELLED,          // Đã hủy
    EXPIRED,            // Hết hạn (quá thời gian thanh toán)
    REFUNDED            // Đã hoàn tiền
}
