package com.saga.payment.repository;

import com.saga.payment.model.entity.ProcessedEvent;

import java.time.Duration;
import java.util.List;

public interface ProcessedEventRepositoryCustom {
    List<ProcessedEvent> claimBatch(int limit, Duration lease);
}
