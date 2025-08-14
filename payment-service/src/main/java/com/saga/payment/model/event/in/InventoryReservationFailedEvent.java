package com.saga.payment.model.event.in;

import com.saga.payment.model.event.Event;

import java.util.UUID;

public record InventoryReservationFailedEvent(UUID orderId, String productId, String reason, UUID eventId, UUID traceId) implements Event {
    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public UUID getKey() {
        return orderId;
    }
}
