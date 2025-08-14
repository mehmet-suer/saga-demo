package com.saga.streamer.model.event;

import java.util.UUID;

public record OrderPaymentFailedEvent(UUID orderId, String userId, String reason, UUID eventId, UUID traceId) {}
