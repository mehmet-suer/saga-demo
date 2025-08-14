package com.saga.order.model.event.in;

import java.util.UUID;

public record InventoryReservationFailedEvent(UUID orderId, String productId, String reason, UUID eventId,
                                              UUID traceId) {
}
