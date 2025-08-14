package com.saga.streamer.model.event;

public record OrderStatus(String orderId, String paymentStatus, String inventoryStatus, String finalStatus) {
}
