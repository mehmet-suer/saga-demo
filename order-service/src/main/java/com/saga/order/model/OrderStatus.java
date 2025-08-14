package com.saga.order.model;

public enum OrderStatus {
    CREATED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    CANCELLED
}
