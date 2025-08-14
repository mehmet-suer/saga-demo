package com.saga.payment.repository;

import com.saga.payment.model.EventType;
import com.saga.payment.model.entity.IdempotentEvent;
import com.saga.payment.model.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdempotentEventRepository extends JpaRepository<IdempotentEvent, Long> {
    Optional<IdempotentEvent> findByEventIdAndEventType(UUID eventId, EventType eventType);

}