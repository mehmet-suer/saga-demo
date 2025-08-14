package com.saga.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.inventory.config.KafkaTopicProperties;
import com.saga.inventory.exception.OutboxSerializationException;
import com.saga.inventory.model.EventType;
import com.saga.inventory.model.constant.KafkaHeaders;
import com.saga.inventory.model.entity.IdempotentEvent;
import com.saga.inventory.model.entity.Inventory;
import com.saga.inventory.model.entity.ProcessedEvent;
import com.saga.inventory.model.event.Event;
import com.saga.inventory.model.event.in.OrderPaymentCompletedEvent;
import com.saga.inventory.model.event.out.InventoryReservationCompletedEvent;
import com.saga.inventory.model.event.out.InventoryReservationFailedEvent;
import com.saga.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository repository;
    private final ProcessedEventService processedEventService;
    private final IdempotentEventService idempotentEventService;
    private final ObjectMapper objectMapper;
    private final KafkaTopicProperties kafkaTopicProperties;

    public InventoryService(InventoryRepository repository,
                            ProcessedEventService processedEventService,
                            IdempotentEventService idempotentEventService,
                            ObjectMapper objectMapper,
                            KafkaTopicProperties kafkaTopicProperties) {
        this.repository = repository;
        this.processedEventService = processedEventService;
        this.idempotentEventService = idempotentEventService;
        this.objectMapper = objectMapper;
        this.kafkaTopicProperties = kafkaTopicProperties;
    }

    public boolean reserve(String productId) {
        return repository.tryReserve(productId) > 0;

    }

    public void initProduct(String productId, int quantity) {
        repository.save(new Inventory(productId, quantity));
    }

    public List<Inventory> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void process(OrderPaymentCompletedEvent event) {
        boolean inventoryReserved = reserve(event.productId());
        persistProcessedEvent(event, inventoryReserved);
        persistIdempotentEventIfInventoryReserved(event, inventoryReserved);

    }

    private void persistIdempotentEventIfInventoryReserved(OrderPaymentCompletedEvent event, boolean inventoryReserved) {
        if (inventoryReserved) {
            idempotentEventService.save(new IdempotentEvent(event.getEventId(), EventType.INVENTORY_RESERVATION_COMPLETED));
        }
    }

    private void persistProcessedEvent(OrderPaymentCompletedEvent event, boolean inventoryReserved) {
        try {
            Event outEvent = getOutEvent(event, inventoryReserved);
            String payload = objectMapper.writeValueAsString(outEvent);
            var headerPayload = getHeadersPayload(event);
            String topic = inventoryReserved ? kafkaTopicProperties.inventoryReservationCompleted() : kafkaTopicProperties.inventoryReservationFailed();
            EventType eventType = inventoryReserved ? EventType.INVENTORY_RESERVATION_COMPLETED: EventType.INVENTORY_RESERVATION_FAILED;
            var processedEvent = new ProcessedEvent(outEvent.getEventId(), eventType, payload, headerPayload, event.getKey(), topic);

            processedEventService.save(processedEvent);
        } catch (JsonProcessingException e) {
            log.error("Inventory processed event error: {} ", event.eventId(), e);
            throw new OutboxSerializationException("Serialization error occurred while persisting processed event ", e);
        }
    }

    private String getHeadersPayload(OrderPaymentCompletedEvent event) throws JsonProcessingException {
        var headers = Map.of(KafkaHeaders.EVENT_ID, event.eventId(), KafkaHeaders.TRACE_ID, event.traceId());
        return objectMapper.writeValueAsString(headers);
    }

    private Event getOutEvent(OrderPaymentCompletedEvent event, boolean inventoryReserved) {
        Event outEvent;
        if (inventoryReserved) {
            outEvent = new InventoryReservationCompletedEvent(event.orderId(), event.productId(), event.eventId(), event.traceId());
        } else {
            outEvent = new InventoryReservationFailedEvent(event.orderId(), event.productId(), "Stock unavailable", event.eventId(), event.traceId());
        }
        return outEvent;
    }
}