package com.saga.common.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public class IdempotentEventService <E extends Enum<E>> {

    private static final Logger log = LoggerFactory.getLogger(IdempotentEventService.class);

    private final IdempotentEventClaimRepository<?, ?> repository;

    public IdempotentEventService(IdempotentEventClaimRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean claim(UUID eventId, E eventType) {
        int inserted = repository.tryInsert(eventId, eventType.name(), Instant.now());
        if (inserted == 0) {
            log.warn("Already processed: eventId={} eventType={}", eventId, eventType);
            return false;
        }
        return true;
    }
}
