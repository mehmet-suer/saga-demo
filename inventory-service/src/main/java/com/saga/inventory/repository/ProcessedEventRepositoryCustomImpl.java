package com.saga.inventory.repository;

import com.saga.common.kafkaoutbox.ProcessedEventStatus;
import com.saga.inventory.model.entity.ProcessedEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
public class ProcessedEventRepositoryCustomImpl implements ProcessedEventRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProcessedEvent> claimBatch(int limit, Duration lease) {
        // Keep this predicate aligned with ProcessedEventStatus and outbox indexes (see enum checklist).
        String sql = """
                WITH batchToClaim AS (
                  SELECT id
                  FROM processed_events
                  WHERE (
                    status = :newStatus
                    OR (status = :inProgressStatus AND locked_until < now())
                    OR (status = :retryStatus AND next_retry_at <= now())
                  )
                  ORDER BY created_at
                  FOR UPDATE SKIP LOCKED
                  LIMIT :limit
                )
                UPDATE processed_events e
                SET status = :inProgressStatus,
                    locked_until = now() + (:leaseSeconds || ' seconds')::interval,
                    retry_count = COALESCE(e.retry_count, 0) + 1
                FROM batchToClaim
                WHERE e.id = batchToClaim.id
                RETURNING e.*;
                """;

        @SuppressWarnings("unchecked")
        List<ProcessedEvent> claimed = entityManager.createNativeQuery(sql, ProcessedEvent.class)
                .setParameter("limit", limit)
                .setParameter("leaseSeconds", Math.toIntExact(lease.getSeconds()))
                .setParameter("newStatus", ProcessedEventStatus.NEW.name())
                .setParameter("inProgressStatus", ProcessedEventStatus.IN_PROGRESS.name())
                .setParameter("retryStatus", ProcessedEventStatus.RETRY.name())
                .getResultList();
        return claimed;
    }
}
