package com.saga.payment.kafka.listener;

import com.saga.payment.model.EventType;
import com.saga.payment.model.event.in.InventoryReservationFailedEvent;
import com.saga.payment.service.IdempotentEventService;
import com.saga.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationFailedListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationFailedListener.class);

    private final PaymentService paymentService;
    private final IdempotentEventService idempotentEventService;

    public InventoryReservationFailedListener(PaymentService orderService, IdempotentEventService idempotentEventService) {
        this.paymentService = orderService;
        this.idempotentEventService = idempotentEventService;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.inventoryReservationFailed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "inventoryReservationFailedEventContainerFactory"
    )
    public void handle(InventoryReservationFailedEvent event) {
        try {
            var eventType = EventType.ORDER_PAYMENT_CANCELLED;
            var processedEvent = idempotentEventService.findByEventIdAndEventType(event.eventId(), eventType);
            if (processedEvent.isPresent()) {
                log.warn("Already successfully revert payment processed, skipping: {} ", event.eventId());
                return;
            }
            paymentService.processPaymentCancellation(event, eventType);
        } catch (Exception e) {
            log.error("Payment revert processing error: {}", event.eventId(), e);
            throw e;
        }
    }
}
