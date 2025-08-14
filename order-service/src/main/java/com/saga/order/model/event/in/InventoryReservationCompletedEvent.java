package com.saga.order.model.event.in;

import java.util.UUID;

public record InventoryReservationCompletedEvent(UUID orderId, String productId, UUID eventId, UUID traceId) {
}