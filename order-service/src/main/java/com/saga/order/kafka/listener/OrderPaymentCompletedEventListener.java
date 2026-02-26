package com.saga.order.kafka.listener;

import com.saga.order.model.event.in.OrderPaymentCompletedEvent;
import com.saga.order.service.InboundOrderEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentCompletedEventListener.class);
    private final InboundOrderEventService inboundOrderEventService;

    public OrderPaymentCompletedEventListener(InboundOrderEventService inboundOrderEventService) {
        this.inboundOrderEventService = inboundOrderEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.orderPaymentCompleted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderPaymentCompletedEventContainerFactory")
    public void handle(OrderPaymentCompletedEvent event) {
        try {
            inboundOrderEventService.handle(event);
        } catch (Exception e) {
            log.error("Order payment completed event processing failed: {} ", event.eventId(), e);
            throw e;
        }
    }
}
