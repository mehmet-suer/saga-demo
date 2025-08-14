package com.saga.order.kafka.listener;

import com.saga.order.model.EventType;
import com.saga.order.model.event.in.OrderPaymentCompletedEvent;
import com.saga.order.service.IdempotentEventService;
import com.saga.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentCompletedEventListener.class);
    private final OrderService orderService;
    private final IdempotentEventService idempotentEventService;

    public OrderPaymentCompletedEventListener(OrderService orderService, IdempotentEventService idempotentEventService) {
        this.orderService = orderService;
        this.idempotentEventService = idempotentEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.orderPaymentCompleted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderPaymentCompletedEventContainerFactory"
    )
    public void handle(OrderPaymentCompletedEvent event) {
        try {
            EventType eventType = EventType.ORDER_PAYMENT_COMPLETED;
            var alreadyProcessed = idempotentEventService.findByIdAndEventType(event.eventId(), eventType);
            if (alreadyProcessed.isPresent()) {
                log.warn("Already processed: {}", event.eventId());
                return;
            }
            orderService.markPaymentCompleted(event, eventType);
        } catch (Exception e) {
            log.error("Order payment completed event processing failed: {} ", event.eventId(), e);
            throw e;

        }
    }
}