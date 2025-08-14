package com.saga.order.repository;

import com.saga.order.model.EventType;
import com.saga.order.model.entity.IdempotentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotentEventRepository extends JpaRepository<IdempotentEvent, UUID> {
    Optional<IdempotentEvent> findByEventIdAndEventType(UUID eventId, EventType eventType);
}