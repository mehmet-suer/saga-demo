package com.saga.payment.service;

import com.saga.common.idempotency.IdempotentEventService;
import com.saga.payment.model.EventType;
import com.saga.payment.model.event.in.InventoryReservationFailedEvent;
import com.saga.payment.model.event.in.OrderCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InboundPaymentEventService {

    private final IdempotentEventService<EventType> idempotentEventService;
    private final PaymentService paymentService;

    public InboundPaymentEventService(IdempotentEventService<EventType> idempotentEventService, PaymentService paymentService) {
        this.idempotentEventService = idempotentEventService;
        this.paymentService = paymentService;
    }

    @Transactional
    public void handle(OrderCreatedEvent event) {
        processIfFirst(event.eventId(), EventType.ORDER_CREATED, () -> paymentService.process(event));
    }

    @Transactional
    public void handle(InventoryReservationFailedEvent event) {
        processIfFirst(event.eventId(), EventType.INVENTORY_RESERVATION_FAILED,
                () -> paymentService.processPaymentCancellation(event));
    }

    private void processIfFirst(UUID eventId, EventType eventType, Runnable action) {
        if (!idempotentEventService.claim(eventId, eventType)) {
            return;
        }
        action.run();
    }
}
