package com.saga.inventory.model.event.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saga.inventory.model.event.Event;

import java.util.UUID;

public record InventoryReservationFailedEvent(UUID orderId, String productId, String reason, UUID eventId,
                                              UUID traceId) implements Event {
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

