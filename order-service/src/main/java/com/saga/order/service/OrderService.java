package com.saga.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.order.config.KafkaTopicProperties;
import com.saga.order.model.EventType;
import com.saga.order.model.InventoryState;
import com.saga.order.model.OrderStatus;
import com.saga.order.model.PaymentState;
import com.saga.common.kafkaoutbox.KafkaHeaders;
import com.saga.order.model.dto.request.CreateOrderRequest;
import com.saga.order.model.entity.Order;
import com.saga.order.model.entity.ProcessedEvent;
import com.saga.order.model.event.in.InventoryReservationCompletedEvent;
import com.saga.order.model.event.in.InventoryReservationFailedEvent;
import com.saga.order.model.event.in.OrderPaymentCompletedEvent;
import com.saga.order.model.event.in.OrderPaymentFailedEvent;
import com.saga.order.model.event.out.OrderCreatedEvent;
import com.saga.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;
    private final KafkaTopicProperties kafkaTopicProperties;

    public OrderService(OrderRepository orderRepository, ProcessedEventService processedEventService, ObjectMapper objectMapper, KafkaTopicProperties kafkaTopicProperties) {
        this.orderRepository = orderRepository;
        this.processedEventService = processedEventService;
        this.objectMapper = objectMapper;
        this.kafkaTopicProperties = kafkaTopicProperties;
    }


    @Transactional
    public void createOrder(CreateOrderRequest req) {
        Order order = new Order(req.orderId(), req.userId(), req.productId());
        order.setStatus(OrderStatus.CREATED);
        orderRepository.save(order);
        persistProcessedEvent(req.orderId(), req.userId(), req.productId(), EventType.ORDER_CREATED);
    }

    private void persistProcessedEvent(UUID orderId, String userId, String productId, EventType eventType) {
        var event = new OrderCreatedEvent(orderId, userId, productId, UUID.randomUUID(), UUID.randomUUID(), Math.abs(orderId.hashCode()) % 20);
        String payload = getPayload(event);
        String headerPayload = getHeadersPayload(event);

        String topic = kafkaTopicProperties.orderCreated();
        var processedEvent = new ProcessedEvent(event.eventId(), eventType, payload, headerPayload, event.key(), topic);
        processedEventService.save(processedEvent);
    }

    private String getPayload(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for eventId={} traceId={}", event.eventId(), event.traceId(), e);
            throw new RuntimeException("Failed to serialize event ", e);
        }
    }

    private String getHeadersPayload(OrderCreatedEvent event) {
        try {
            var headers = Map.of(KafkaHeaders.EVENT_ID, event.eventId(), KafkaHeaders.TRACE_ID, event.traceId());
            return objectMapper.writeValueAsString(headers);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Kafka headers for eventId={} traceId={}", event.eventId(), event.traceId(), e);
            throw new RuntimeException("Failed to serialize Kafka headers", e);
        }
    }

    @Transactional
    public void markPaymentCompleted(OrderPaymentCompletedEvent event) {
        markPaymentCompleted(event.orderId());
    }

    private void markPaymentCompleted(UUID orderId) {
        Order order = getOrder(orderId);
        order.markPaymentCompleted();
        orderRepository.save(order);
    }

    @Transactional
    public void markPaymentFailed(OrderPaymentFailedEvent event) {
        applyPaymentFailed(event);
    }

    private void applyPaymentFailed(OrderPaymentFailedEvent event) {
        Order order = getOrder(event.orderId());
        order.markPaymentFailed(event.reason());
        orderRepository.save(order);
    }


    @Transactional
    public void processInventoryReserved(InventoryReservationCompletedEvent event) {
        markInventoryReserved(event);
    }

    private void markInventoryReserved(InventoryReservationCompletedEvent event) {
        Order order = getOrder(event.orderId());
        order.markInventoryReserved();
        orderRepository.save(order);
    }

    @Transactional
    public void processInventoryReservationFailed(InventoryReservationFailedEvent event) {
        markInventoryFailed(event);
    }

    private void markInventoryFailed(InventoryReservationFailedEvent event) {
        Order order = getOrder(event.orderId());
        order.markInventoryFailed(event.reason());
        orderRepository.save(order);
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }


    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public List<Order> findTimeoutCandidatesBeforeCutoff(PaymentState pendingPaymentState, PaymentState completedPaymentState, InventoryState pendingInventoryState, Instant cutoff, Pageable pageable) {
        return orderRepository.findTimeoutCandidatesBeforeCutoff(pendingPaymentState, completedPaymentState, pendingInventoryState, cutoff, pageable);
    }

    @Transactional
    public void markTimedOut(Order order, OrderStatus orderStatus, String reason, Instant timeoutTime, List<OrderStatus> terminalStates, PaymentState pendingPaymentState, PaymentState completedPaymentState, InventoryState pendingInventoryState, Instant cutoff) {
        int updated = orderRepository.markTimedOut(order.getId(), order.getVersion(), orderStatus, reason, timeoutTime, terminalStates, pendingPaymentState, completedPaymentState, pendingInventoryState, cutoff);
        if (updated == 0) {
            log.info("Timed-out update skipped for orderId={} version={} status={} paymentState={} inventoryState={}", order.getId(), order.getVersion(), order.getStatus(), order.getPaymentState(), order.getInventoryState());
        }
    }
}