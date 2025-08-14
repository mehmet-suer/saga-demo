package com.saga.order.kafka.listener;

import com.saga.order.model.EventType;
import com.saga.order.model.event.in.OrderPaymentFailedEvent;
import com.saga.order.service.IdempotentEventService;
import com.saga.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentFailedEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationFailedEventListener.class);
    private final OrderService orderService;
    private final IdempotentEventService idempotentEventService;

    public OrderPaymentFailedEventListener(OrderService orderService, IdempotentEventService idempotentEventService) {
        this.orderService = orderService;
        this.idempotentEventService = idempotentEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.orderPaymentFailed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderPaymentFailedEventContainerFactory"
    )
    public void handle(OrderPaymentFailedEvent event) {
        try {
            EventType eventType = EventType.ORDER_PAYMENT_FAILED;
            var alreadyProcessed = idempotentEventService.findByIdAndEventType(event.eventId(), eventType);
            if (alreadyProcessed.isPresent()) {
                log.warn("Already processed: {}", event.eventId());
                return;
            }
            orderService.markPaymentFailed(event, eventType);
        } catch (Exception e) {
            log.error("Order payment failed event processing failed: {} ", event.eventId(), e);
            throw e;
        }
    }
}
