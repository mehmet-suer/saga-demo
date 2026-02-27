package com.saga.common.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

@NoRepositoryBean
public interface IdempotentEventClaimRepository<T, ID> extends JpaRepository<T, ID> {

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
