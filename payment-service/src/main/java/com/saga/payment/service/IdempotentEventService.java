package com.saga.payment.service;

import com.saga.payment.model.EventType;
import com.saga.payment.model.entity.IdempotentEvent;
import com.saga.payment.repository.IdempotentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotentEventService {
    private static final Logger log = LoggerFactory.getLogger(IdempotentEventService.class);

    private final IdempotentEventRepository repository;

    public IdempotentEventService(IdempotentEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(IdempotentEvent idempotentEvent) {
        repository.save(idempotentEvent);
    }

    public Optional<IdempotentEvent> findByEventIdAndEventType(UUID eventId, EventType eventType) {
        return repository.findByEventIdAndEventType(eventId, eventType);
    }
}