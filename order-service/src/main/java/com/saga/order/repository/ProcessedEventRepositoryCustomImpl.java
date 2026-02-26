package com.saga.order.repository;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.saga.order.model.entity.ProcessedEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class ProcessedEventRepositoryCustomImpl implements ProcessedEventRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ProcessedEvent> claimBatch(int limit, Duration lease) {
        // Keep this predicate aligned with ProcessedEventStatus and outbox indexes (see enum checklist).
        String sql = """
                WITH batchToClaim AS (
                  SELECT id
                  FROM processed_events
                  WHERE (
                    status = 'NEW'
                    OR (status = 'IN_PROGRESS' AND locked_until < now())
                    OR (status = 'RETRY' AND next_retry_at <= now())
                  )
                  ORDER BY created_at
                  FOR UPDATE SKIP LOCKED
                  LIMIT :limit
                )
                UPDATE processed_events e
                SET status = 'IN_PROGRESS',
                    locked_until = now() + (:leaseSeconds || ' seconds')::interval,
                    retry_count = COALESCE(e.retry_count, 0) + 1
                FROM batchToClaim
                WHERE e.id = batchToClaim.id
                RETURNING e.*;
                """;

        @SuppressWarnings("unchecked")
        List<ProcessedEvent> claimed = em.createNativeQuery(sql, ProcessedEvent.class)
                .setParameter("limit", limit)
                .setParameter("leaseSeconds", Math.toIntExact(lease.getSeconds()))
                .getResultList();

        return claimed;
    }
}
