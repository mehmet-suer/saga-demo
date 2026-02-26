package com.saga.order.kafka.listener;

import com.saga.order.model.event.in.InventoryReservationFailedEvent;
import com.saga.order.service.InboundOrderEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationFailedEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationFailedEventListener.class);

    private final InboundOrderEventService inboundOrderEventService;

    public InventoryReservationFailedEventListener(InboundOrderEventService inboundOrderEventService) {
        this.inboundOrderEventService = inboundOrderEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.inventoryReservationFailed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryReservationFailedEventContainerFactory")
    public void handle(InventoryReservationFailedEvent event) {
        try {
            inboundOrderEventService.handle(event);
        } catch (Exception e) {
            log.error("Inventory reservation failed event processing failed: {} ", event.eventId(), e);
            throw e;
        }
    }
}
