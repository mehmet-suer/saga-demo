package com.saga.payment.model.event.in;

import com.saga.payment.model.event.Event;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, String userId, String productId, UUID eventId, UUID traceId, @Min(1) int quantity)  implements Event {
    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public UUID getKey() {
        return orderId;
    }
}
