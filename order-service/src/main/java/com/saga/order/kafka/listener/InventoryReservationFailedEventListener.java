package com.saga.order.kafka.listener;

import com.saga.order.model.EventType;
import com.saga.order.model.event.in.InventoryReservationFailedEvent;
import com.saga.order.service.IdempotentEventService;
import com.saga.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationFailedEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationFailedEventListener.class);

    private final OrderService orderService;
    private final IdempotentEventService idempotentEventService;

    public InventoryReservationFailedEventListener(OrderService orderService, IdempotentEventService idempotentEventService) {
        this.orderService = orderService;
        this.idempotentEventService = idempotentEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.inventoryReservationFailed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryReservationFailedEventContainerFactory"
    )
    public void handle(InventoryReservationFailedEvent event) {
        try {
            EventType eventType = EventType.INVENTORY_RESERVATION_FAILED;
            var alreadyProcessed = idempotentEventService.findByIdAndEventType(event.eventId(), eventType);
            if (alreadyProcessed.isPresent()) {
                log.warn("Already processed: {}", event.eventId());
                return;
            }

            orderService.processInventoryReservationFailed(event, eventType);
        } catch (Exception e) {
            log.error("Inventory reservation failed event processing failed: {} ", event.eventId(), e);
            throw e;

        }
    }
}
