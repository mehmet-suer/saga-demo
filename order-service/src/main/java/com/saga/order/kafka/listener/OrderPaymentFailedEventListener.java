package com.saga.order.kafka.listener;

import com.saga.order.model.event.in.OrderPaymentFailedEvent;
import com.saga.order.service.InboundOrderEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentFailedEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentFailedEventListener.class);
    private final InboundOrderEventService inboundOrderEventService;

    public OrderPaymentFailedEventListener(InboundOrderEventService inboundOrderEventService) {
        this.inboundOrderEventService = inboundOrderEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.orderPaymentFailed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderPaymentFailedEventContainerFactory")
    public void handle(OrderPaymentFailedEvent event) {
        try {
            inboundOrderEventService.handle(event);
        } catch (Exception e) {
            log.error("Order payment failed event processing failed: {} ", event.eventId(), e);
            throw e;
        }
    }
}
