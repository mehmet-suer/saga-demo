package com.saga.payment.model.event.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saga.payment.model.event.Event;

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
