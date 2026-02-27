package com.saga.order.repository;

import com.saga.order.model.entity.ProcessedEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long>, ProcessedEventRepositoryCustom {

  @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
  @Query("select e from ProcessedEvent e where e.id = :id")
  Optional<ProcessedEvent> findForUpdate(@Param("id") Long id);
}
