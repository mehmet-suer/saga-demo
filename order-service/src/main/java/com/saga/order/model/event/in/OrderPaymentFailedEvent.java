package com.saga.order.model.event.in;

import java.util.UUID;

public record OrderPaymentFailedEvent(UUID orderId, String userId, String reason, UUID eventId, UUID traceId) {}
