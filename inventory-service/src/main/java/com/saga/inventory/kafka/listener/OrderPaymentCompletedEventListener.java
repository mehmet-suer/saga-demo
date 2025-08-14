package com.saga.inventory.kafka.listener;

import com.saga.inventory.model.EventType;
import com.saga.inventory.model.event.in.OrderPaymentCompletedEvent;
import com.saga.inventory.service.IdempotentEventService;
import com.saga.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentCompletedEventListener.class);

    private final InventoryService inventoryService;
    private final IdempotentEventService idempotentEventService;

    public OrderPaymentCompletedEventListener(InventoryService inventoryService, IdempotentEventService idempotentEventService) {
        this.inventoryService = inventoryService;
        this.idempotentEventService = idempotentEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.orderPaymentCompleted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderPaymentCompletedEventKafkaListenerFactory"
    )
    public void handle(OrderPaymentCompletedEvent event) {
        try {
            var eventType = EventType.INVENTORY_RESERVATION_COMPLETED;
            var processedEvent = idempotentEventService.findByEventIdAndEventType(event.eventId(), eventType);
            if (processedEvent.isPresent()) {
                log.warn("Already successfully processed, skipping: {} ", event.eventId());
                return;
            }
            inventoryService.process(event);
        } catch (Exception e) {
            log.error("Inventory reservation failed:  {}", event.eventId(), e);
            throw e;
        }
    }
}