package com.saga.inventory.model.event.in;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saga.inventory.model.event.Event;

import java.util.UUID;

public record OrderPaymentCompletedEvent(UUID orderId, String userId, String productId, UUID eventId, UUID traceId) implements Event {
    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    @JsonIgnore
    public UUID getKey() {
        return orderId;
    }
}
