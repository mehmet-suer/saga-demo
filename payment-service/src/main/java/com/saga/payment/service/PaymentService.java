package com.saga.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.payment.config.KafkaTopicProperties;
import com.saga.payment.exception.OutboxSerializationException;
import com.saga.payment.model.EventType;
import com.saga.payment.model.PaymentStatus;
import com.saga.payment.model.constant.KafkaHeaders;
import com.saga.payment.model.entity.IdempotentEvent;
import com.saga.payment.model.entity.Payment;
import com.saga.payment.model.entity.ProcessedEvent;
import com.saga.payment.model.event.Event;
import com.saga.payment.model.event.in.InventoryReservationFailedEvent;
import com.saga.payment.model.event.in.OrderCreatedEvent;
import com.saga.payment.model.event.out.OrderPaymentCompletedEvent;
import com.saga.payment.model.event.out.OrderPaymentFailedEvent;
import com.saga.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository repository;
    private final ProcessedEventService processedEventService;

    private final IdempotentEventService idempotentEventService;
    private final ObjectMapper objectMapper;
    private final KafkaTopicProperties kafkaTopicProperties;

    private final Random random = new Random();

    public PaymentService(PaymentRepository repository, ProcessedEventService processedEventService, IdempotentEventService idempotentEventService, ObjectMapper objectMapper, KafkaTopicProperties kafkaTopicProperties) {
        this.repository = repository;
        this.processedEventService = processedEventService;
        this.idempotentEventService = idempotentEventService;
        this.objectMapper = objectMapper;
        this.kafkaTopicProperties = kafkaTopicProperties;
    }

    public void save(UUID orderId, String userId, PaymentStatus status, String reason) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setStatus(status);
        payment.setReason(reason);
        repository.save(payment);
    }


    @Transactional(rollbackFor = Exception.class)
    public void process(OrderCreatedEvent event, EventType eventType) {
        PaymentResult paymentResult = makePayment();
        persistPayment(event, paymentResult);
        persistProcessedEvent(event, paymentResult);
        persistIdempotentEventIfPaymentSucceeded(event, paymentResult, eventType);
    }

    private void persistIdempotentEventIfPaymentSucceeded(OrderCreatedEvent event, PaymentResult paymentResult, EventType eventType) {
        if (paymentResult.isSuccess) {
            idempotentEventService.save(new IdempotentEvent(event.eventId(), eventType));
        }
    }

    private void persistPayment(OrderCreatedEvent event, PaymentResult paymentResult) {
        save(event.orderId(), event.userId(), paymentResult.isSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED, paymentResult.failureReason);
    }

    private void persistProcessedEvent(OrderCreatedEvent event,
                                       PaymentResult paymentResult) {
        try {
            Event outEvent = getOutEvent(event, paymentResult);
            String payload = objectMapper.writeValueAsString(outEvent);
            String headerPayload = getHeadersPayload(event);
            EventType eventType = paymentResult.isSuccess ? EventType.ORDER_PAYMENT_COMPLETED : EventType.ORDER_PAYMENT_FAILED;
            String topic = paymentResult.isSuccess ? kafkaTopicProperties.orderPaymentCompleted() : kafkaTopicProperties.orderPaymentFailed();
            var processedEvent = new ProcessedEvent(outEvent.getEventId(), eventType, payload, headerPayload, event.getKey(), topic);
            processedEventService.save(processedEvent);

        } catch (JsonProcessingException e) {
            log.error("Payment processing error: {} ", event.eventId(), e);
            throw new OutboxSerializationException("Serialization error occurred while persisting processed event ", e);
        }
    }

    private String getHeadersPayload(OrderCreatedEvent event) throws JsonProcessingException {
        var headers = Map.of(KafkaHeaders.EVENT_ID, event.eventId(), KafkaHeaders.TRACE_ID, event.traceId());

        return objectMapper.writeValueAsString(headers);
    }

    private Event getOutEvent(OrderCreatedEvent event, PaymentResult paymentResult) {
        Event outEvent;
        if (paymentResult.isSuccess) {
            outEvent = new OrderPaymentCompletedEvent(event.orderId(), event.userId(), event.productId(), event.eventId(), event.traceId());
        } else {
            outEvent = new OrderPaymentFailedEvent(event.orderId(), event.userId(), paymentResult.failureReason, event.eventId(), event.traceId());
        }
        return outEvent;
    }

    private PaymentResult makePayment() {
        var success = random.nextInt(100) < 70;
        var reason = success ? null : "Insufficient funds";
        return new PaymentResult(success, reason);
    }

    public List<Payment> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void processPaymentCancellation(InventoryReservationFailedEvent event, EventType eventType) {
        var paymentCancellationResult = cancelPayment(event);
        if (paymentCancellationResult.success) {
            var idempotentEvent = new IdempotentEvent(event.eventId(), eventType);
            idempotentEventService.save(idempotentEvent);
        } else {
            log.warn("Payment cancellation failed for eventId: {} - Reason: {}", event.eventId(), paymentCancellationResult.failureReason);
        }
    }


    private PaymentCancellationResult cancelPayment(InventoryReservationFailedEvent event) {
        var paymentOpt = repository.findByOrderId(event.orderId());
        if (paymentOpt.isEmpty()) {
            log.warn("No payment found to cancel for orderId = {}", event.orderId());
            return new PaymentCancellationResult(false, "Payment not found");
        }
        var payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.CANCELLED);
        repository.save(payment);
        log.warn("Payment for Order ({}) canceled because of {}", event.orderId(), event.reason());
        return new PaymentCancellationResult(true, null);
    }

    record PaymentResult(boolean isSuccess, String failureReason) {
    }

    record PaymentCancellationResult(boolean success, String failureReason) {
    }
}