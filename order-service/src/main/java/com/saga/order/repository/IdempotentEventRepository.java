package com.saga.order.repository;

import com.saga.order.model.entity.IdempotentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface IdempotentEventRepository extends JpaRepository<IdempotentEvent, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO idempotent_events (event_id, event_type, processed_at)
            VALUES (:eventId, :eventType, :processedAt)
            ON CONFLICT (event_id, event_type) DO NOTHING
            """, nativeQuery = true)
    int tryInsert(@Param("eventId") UUID eventId,
                  @Param("eventType") String eventType,
                  @Param("processedAt") Instant processedAt);
}