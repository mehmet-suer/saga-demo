package com.saga.order.model.event.in;

import java.util.UUID;

public record OrderPaymentCompletedEvent(UUID orderId, String userId, UUID eventId, UUID traceId) {}
