package com.saga.order.repository;

import com.saga.order.model.entity.ProcessedEvent;

import java.time.Duration;
import java.util.List;

public interface ProcessedEventRepositoryCustom {
    List<ProcessedEvent> claimBatch(int limit, Duration lease);
}
