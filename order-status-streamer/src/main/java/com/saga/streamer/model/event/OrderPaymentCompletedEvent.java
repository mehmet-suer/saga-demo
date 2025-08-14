package com.saga.streamer.model.event;

import java.util.UUID;

public record OrderPaymentCompletedEvent(UUID orderId, String userId, UUID eventId, UUID traceId) {}
