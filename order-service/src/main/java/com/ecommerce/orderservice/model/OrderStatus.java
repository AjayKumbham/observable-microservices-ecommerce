package com.ecommerce.orderservice.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_FAILED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
