package com.saga.order.kafka.listener;

import com.saga.order.model.EventType;
import com.saga.order.model.event.in.InventoryReservationCompletedEvent;
import com.saga.order.service.IdempotentEventService;
import com.saga.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationCompletedEventListener.class);

    private final OrderService orderService;
    private final IdempotentEventService idempotentEventService;

    public InventoryReservationCompletedEventListener(OrderService orderService, IdempotentEventService idempotentEventService) {
        this.orderService = orderService;
        this.idempotentEventService = idempotentEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.inventoryReservationCompleted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryReservationCompletedEventContainerFactory"
    )
    public void handle(InventoryReservationCompletedEvent event) {
        try {
            EventType eventType = EventType.INVENTORY_RESERVATION_COMPLETED;
            var alreadyProcessed = idempotentEventService.findByIdAndEventType(event.eventId(), eventType);
            if (alreadyProcessed.isPresent()) {
                log.warn("Already processed: {}", event.eventId());
                return;
            }
            orderService.processInventoryReserved(event, eventType);
        } catch (Exception e) {
            log.error("Inventory reservation completed event processing failed: {} ", event.eventId(), e);
            throw e;

        }
    }
}