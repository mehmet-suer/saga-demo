package com.saga.order.model.event.out;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String userId,
        String productId,
        UUID eventId,
        UUID traceId,
        int quantity
) {
    @JsonIgnore
    public UUID key() {
        return orderId;
    }

}