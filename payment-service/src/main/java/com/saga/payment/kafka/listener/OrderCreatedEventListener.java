package com.saga.payment.kafka.listener;

import com.saga.payment.model.EventType;
import com.saga.payment.model.event.in.OrderCreatedEvent;
import com.saga.payment.service.IdempotentEventService;
import com.saga.payment.service.PaymentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OrderCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventListener.class);

    private final PaymentService paymentService;
    private final IdempotentEventService idempotentEventService;
    private final Validator validator;

    public OrderCreatedEventListener(PaymentService paymentService, IdempotentEventService idempotentEventService, Validator validator) {

        this.paymentService = paymentService;
        this.idempotentEventService = idempotentEventService;
        this.validator = validator;
    }

    @KafkaListener(
            topics = "#{@kafkaTopicProperties.orderCreated}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderCreatedEventConsumerFactory"
    )
    public void handle(OrderCreatedEvent event) {
        validate(event);
        try {
            var eventType = EventType.ORDER_PAYMENT_COMPLETED;
            var idempotentEvent = idempotentEventService.findByEventIdAndEventType(event.eventId(), eventType);
            if (idempotentEvent.isPresent()) {
                log.warn("Already successfully processed, skipping: {} " , event.eventId());
                return;
            }
            paymentService.process(event, eventType);
        } catch (Exception e) {
            log.error("Payment processing error: {} ", event.eventId(),  e);
            throw e;
        }
    }


    private void validate(OrderCreatedEvent event) {
        Set<ConstraintViolation<OrderCreatedEvent>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            log.warn("Validation failed for event {}: {}", event.eventId(), violations);
            throw new ConstraintViolationException(violations);
        }
    }


}