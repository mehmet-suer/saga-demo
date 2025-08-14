package com.saga.inventory.repository;

import com.saga.inventory.model.EventType;
import com.saga.inventory.model.entity.IdempotentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotentEventRepository extends JpaRepository<IdempotentEvent, Long> {
    Optional<IdempotentEvent> findByEventIdAndEventType(UUID eventId, EventType eventType);


}