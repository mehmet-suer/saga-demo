package com.saga.inventory.kafka.listener;

import com.saga.inventory.model.event.in.OrderPaymentCompletedEvent;
import com.saga.inventory.service.InboundInventoryEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentCompletedEventListener.class);

    private final InboundInventoryEventService inboundInventoryEventService;

    public OrderPaymentCompletedEventListener(InboundInventoryEventService inboundInventoryEventService) {
        this.inboundInventoryEventService = inboundInventoryEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.orderPaymentCompleted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderPaymentCompletedEventKafkaListenerFactory"
    )
    public void handle(OrderPaymentCompletedEvent event) {
        try {
            inboundInventoryEventService.handle(event);
        } catch (Exception e) {
            log.error("Inventory reservation failed:  {}", event.eventId(), e);
            throw e;
        }
    }
}
