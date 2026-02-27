package com.saga.inventory.repository;

import com.saga.inventory.model.entity.ProcessedEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long>, ProcessedEventRepositoryCustom {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select e from ProcessedEvent e where e.id = :id")
    Optional<ProcessedEvent> findForUpdate(@Param("id") Long id);

    @Query("""
            SELECT p FROM ProcessedEvent p
            WHERE p.sent = false
              AND (p.lastTriedAt IS NULL OR p.lastTriedAt <= :cutoff)
            ORDER BY p.processedAt ASC
            """)
    List<ProcessedEvent> findUnsentEventsReady(@Param("cutoff") Instant cutoff, Pageable pageable);
}
