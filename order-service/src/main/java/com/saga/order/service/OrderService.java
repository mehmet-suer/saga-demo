package com.saga.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.order.config.KafkaTopicProperties;
import com.saga.order.model.EventType;
import com.saga.order.model.OrderStatus;
import com.saga.order.model.constant.KafkaHeaders;
import com.saga.order.model.entity.IdempotentEvent;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final IdempotentEventService idempotentEventService;
    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;
    private final KafkaTopicProperties kafkaTopicProperties;

    public OrderService(OrderRepository orderRepository,
                        IdempotentEventService idempotentEventService,
                        ProcessedEventService processedEventService,
                        ObjectMapper objectMapper,
                        KafkaTopicProperties kafkaTopicProperties) {
        this.orderRepository = orderRepository;
        this.processedEventService = processedEventService;
        this.objectMapper = objectMapper;
        this.idempotentEventService = idempotentEventService;
        this.kafkaTopicProperties = kafkaTopicProperties;
    }


    @Transactional
    public UUID createOrder(String userId, String productId) {
        UUID orderId = saveOrder(userId, productId);
        EventType eventType = EventType.ORDER_CREATED;
        persistProcessedEvent(userId, productId, orderId, eventType);
        return orderId;
    }

    public UUID saveOrder(String userId, String productId) {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, userId, productId);
        order.setStatus(OrderStatus.CREATED);
        orderRepository.save(order);
        return orderId;
    }

    private void persistProcessedEvent(String userId,
                                       String productId,
                                       UUID orderId,
                                       EventType eventType) {
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, productId, UUID.randomUUID(), UUID.randomUUID(), Math.abs(orderId.hashCode()) % 20);
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
    public void markPaymentCompleted(OrderPaymentCompletedEvent event, EventType eventType) {
        markPaymentCompleted(event.orderId());
        persistIdempotent(event.eventId(), eventType);
    }

    private void markPaymentCompleted(UUID orderId) {
        Order order = getOrder(orderId);
        order.markPaymentCompleted();
        orderRepository.save(order);
    }

    @Transactional
    public void markPaymentFailed(OrderPaymentFailedEvent event, EventType eventType) {
        markPaymentFailed(event);
        persistIdempotent(event.eventId(), eventType);
    }

    private void markPaymentFailed(OrderPaymentFailedEvent event) {
        Order order = getOrder(event.orderId());
        order.markPaymentFailed(event.reason());
        orderRepository.save(order);
    }


    @Transactional
    public void processInventoryReserved(InventoryReservationCompletedEvent event, EventType eventType) {
        markInventoryReserved(event);
        persistIdempotent(event.eventId(), eventType);
    }

    private void markInventoryReserved(InventoryReservationCompletedEvent event) {
        Order order = getOrder(event.orderId());
        order.markInventoryReserved();
        orderRepository.save(order);
    }

    @Transactional
    public void processInventoryReservationFailed(InventoryReservationFailedEvent event, EventType eventType) {
        markInventoryFailed(event);
        persistIdempotent(event.eventId(), eventType);
    }

    private void persistIdempotent(UUID eventId, EventType eventType) {
        var idempotentEvent = new IdempotentEvent(eventId, eventType);
        idempotentEventService.save(idempotentEvent);
    }

    private void markInventoryFailed(InventoryReservationFailedEvent event) {
        Order order = getOrder(event.orderId());
        order.markInventoryFailed(event.reason());
        orderRepository.save(order);
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }


    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public List<Order> findPendingOrdersBeforeCutoff(Collection<OrderStatus> pendingStatuses,
                                                     Instant cutoff,
                                                     Pageable pageable) {
        return orderRepository.findPendingOrdersBeforeCutoff(pendingStatuses, cutoff, pageable);
    }

    @Transactional
    public void markTimedOut(Order  order,  OrderStatus orderStatus, String reason, Instant timeoutTime, List<OrderStatus> pendingStates, Instant cutoff) {
        orderRepository.markTimedOut(order.getId(), order.getVersion(), orderStatus, reason, timeoutTime, pendingStates, cutoff);
    }
}