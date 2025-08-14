package com.saga.streamer.model.event;

import java.util.UUID;

public record InventoryReservationFailedEvent(UUID orderId, String productId, String reason, UUID eventId, UUID traceId) {

}
