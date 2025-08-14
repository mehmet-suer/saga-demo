package com.saga.payment.repository;

import com.saga.payment.model.entity.ProcessedEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    @Query("""
            SELECT p FROM ProcessedEvent p
            WHERE p.sent = false
            ORDER BY p.processedAt ASC
            """)
    List<ProcessedEvent> findUnsent(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
               UPDATE ProcessedEvent e
                  SET e.sent = true,
                      e.lastTriedAt = :now,
                      e.version = e.version + 1
                WHERE e.id = :id
                  AND e.version = :expectedVersion
                  AND e.sent = false
            """)
    int markSentWithVersion(@Param("id") Long id,
                            @Param("expectedVersion") int expectedVersion,
                            @Param("now") Instant now);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
               UPDATE ProcessedEvent e
                  SET e.retryCount = e.retryCount + 1,
                      e.lastTriedAt = :now
                WHERE e.id = :id
                  AND e.sent = false
            """)
    int bumpRetry(@Param("id") Long id, @Param("now") Instant now);
}