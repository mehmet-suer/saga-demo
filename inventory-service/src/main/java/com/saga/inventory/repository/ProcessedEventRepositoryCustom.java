package com.saga.inventory.repository;

import com.saga.inventory.model.entity.ProcessedEvent;

import java.time.Duration;
import java.util.List;

public interface ProcessedEventRepositoryCustom {
    List<ProcessedEvent> claimBatch(int limit, Duration lease);
}
