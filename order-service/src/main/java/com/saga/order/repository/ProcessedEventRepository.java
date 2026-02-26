package com.saga.order.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saga.order.model.entity.ProcessedEvent;

import jakarta.persistence.LockModeType;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long>, ProcessedEventRepositoryCustom {

  @Query("""
      SELECT p FROM ProcessedEvent p
      WHERE p.sent = false
        AND (p.lastTriedAt IS NULL OR p.lastTriedAt <= :cutoff)
      ORDER BY p.processedAt ASC
      """)
  List<ProcessedEvent> findUnsentEventsReady(@Param("cutoff") Instant cutoff, Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
  @Query("select e from ProcessedEvent e where e.id = :id")
  Optional<ProcessedEvent> findForUpdate(@Param("id") Long id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
         UPDATE ProcessedEvent e
            SET e.lastTriedAt = :now
          WHERE e.id = :id
            AND e.sent = false
            AND (e.lastTriedAt IS NULL OR e.lastTriedAt <= :cutoff)
      """)
  int claimForSend(@Param("id") Long id, @Param("now") Instant now, @Param("cutoff") Instant cutoff);
}
