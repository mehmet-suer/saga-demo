package com.saga.order.kafka.listener;

import com.saga.order.model.event.in.InventoryReservationCompletedEvent;
import com.saga.order.service.InboundOrderEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationCompletedEventListener.class);

    private final InboundOrderEventService inboundOrderEventService;

    public InventoryReservationCompletedEventListener(InboundOrderEventService inboundOrderEventService) {
        this.inboundOrderEventService = inboundOrderEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.inventoryReservationCompleted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryReservationCompletedEventContainerFactory"
    )
    public void handle(InventoryReservationCompletedEvent event) {
        try {
            inboundOrderEventService.handle(event);
        } catch (Exception e) {
            log.error("Inventory reservation completed event processing failed: {} ", event.eventId(), e);
            throw e;
        }
    }
}
