package com.saga.inventory.service;

import com.saga.common.idempotency.IdempotentEventService;
import com.saga.inventory.model.EventType;
import com.saga.inventory.model.event.in.OrderPaymentCompletedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InboundInventoryEventService {

    private final IdempotentEventService<EventType> idempotentEventService;
    private final InventoryService inventoryService;

    public InboundInventoryEventService(IdempotentEventService<EventType> idempotentEventService,
                                        InventoryService inventoryService) {
        this.idempotentEventService = idempotentEventService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public void handle(OrderPaymentCompletedEvent event) {
        processIfFirst(event.eventId(), EventType.ORDER_PAYMENT_COMPLETED, () -> inventoryService.process(event));
    }

    private void processIfFirst(UUID eventId, EventType eventType, Runnable action) {
        if (!idempotentEventService.claim(eventId, eventType)) {
            return;
        }
        action.run();
    }
}
