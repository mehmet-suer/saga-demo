package com.saga.streamer.model.event;

import java.util.UUID;

public record InventoryReservationCompletedEvent(UUID orderId, String productId, UUID eventId, UUID traceId) {
}