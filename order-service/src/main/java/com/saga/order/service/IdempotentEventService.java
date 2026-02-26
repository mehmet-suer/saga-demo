package com.saga.order.service;

import com.saga.order.model.EventType;
import com.saga.order.repository.IdempotentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class IdempotentEventService {
    private static final Logger log = LoggerFactory.getLogger(IdempotentEventService.class);

    private final IdempotentEventRepository repository;

    public IdempotentEventService(IdempotentEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean claim(UUID eventId, EventType eventType) {
        int inserted = repository.tryInsert(eventId, eventType.name(), Instant.now());
        if (inserted == 0) {
            log.warn("Already processed: eventId={} eventType={}", eventId, eventType);
            return false;
        }
        return true;
    }
}
