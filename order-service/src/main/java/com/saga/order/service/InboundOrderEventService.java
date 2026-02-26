package com.saga.order.service;

import com.saga.order.model.EventType;
import com.saga.order.model.event.in.InventoryReservationCompletedEvent;
import com.saga.order.model.event.in.InventoryReservationFailedEvent;
import com.saga.order.model.event.in.OrderPaymentCompletedEvent;
import com.saga.order.model.event.in.OrderPaymentFailedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InboundOrderEventService {

    private final IdempotentEventService idempotentEventService;
    private final OrderService orderService;

    public InboundOrderEventService(IdempotentEventService idempotentEventService, OrderService orderService) {
        this.idempotentEventService = idempotentEventService;
        this.orderService = orderService;
    }

    @Transactional
    public void handle(OrderPaymentCompletedEvent event) {
        processIfFirst(event.eventId(), EventType.ORDER_PAYMENT_COMPLETED, () -> orderService.markPaymentCompleted(event));
    }

    @Transactional
    public void handle(OrderPaymentFailedEvent event) {
        processIfFirst(event.eventId(), EventType.ORDER_PAYMENT_FAILED, () -> orderService.markPaymentFailed(event));
    }

    @Transactional
    public void handle(InventoryReservationCompletedEvent event) {
        processIfFirst(event.eventId(), EventType.INVENTORY_RESERVATION_COMPLETED, () -> orderService.processInventoryReserved(event));
    }

    @Transactional
    public void handle(InventoryReservationFailedEvent event) {
        processIfFirst(event.eventId(), EventType.INVENTORY_RESERVATION_FAILED, () -> orderService.processInventoryReservationFailed(event));
    }

    private void processIfFirst(UUID eventId, EventType eventType, Runnable action) {
        if (!idempotentEventService.claim(eventId, eventType)) {
            return;
        }
        action.run();
    }
}
