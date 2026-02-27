package com.saga.payment.kafka.listener;

import com.saga.payment.model.event.in.InventoryReservationFailedEvent;
import com.saga.payment.service.InboundPaymentEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationFailedListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationFailedListener.class);

    private final InboundPaymentEventService inboundPaymentEventService;

    public InventoryReservationFailedListener(InboundPaymentEventService inboundPaymentEventService) {
        this.inboundPaymentEventService = inboundPaymentEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.inventoryReservationFailed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryReservationFailedEventContainerFactory"
    )
    public void handle(InventoryReservationFailedEvent event) {
        try {
            inboundPaymentEventService.handle(event);
        } catch (Exception e) {
            log.error("Payment revert processing error: {}", event.eventId(), e);
            throw e;
        }
    }
}
