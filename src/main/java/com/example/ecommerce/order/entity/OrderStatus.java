package com.example.ecommerce.order.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAID,
    PROCESSING,
    SHIPPED,
    COMPLETED,  
    FAILED,
    CANCELED;

    public String viLabel() {
        return switch (this) {
            case PENDING -> "Chờ xử lý";
            case CONFIRMED -> "Đã xác nhận";
            case PAID -> "Đã thanh toán";
            case PROCESSING -> "Đang xử lý";
            case SHIPPED -> "Đang giao";
            case COMPLETED -> "Hoàn tất";
            case FAILED -> "Thất bại";
            case CANCELED -> "Đã hủy";
        };
    }
}
