package com.saga.order.repository;

import com.saga.order.model.entity.ProcessedEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    @Query("""
            SELECT p FROM ProcessedEvent p
            WHERE p.sent = false
            ORDER BY p.processedAt ASC
            """)
    List<ProcessedEvent> findUnsentEvents();

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select e from ProcessedEvent e where e.id = :id")
    Optional<ProcessedEvent> findForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       UPDATE ProcessedEvent e
          SET e.retryCount = e.retryCount + 1,
              e.lastTriedAt = :lastTriedAt
        WHERE e.id = :id AND e.sent = false
    """)
    int bumpRetry(@Param("id") Long id, @Param("lastTriedAt") Instant lastTriedAt);
}