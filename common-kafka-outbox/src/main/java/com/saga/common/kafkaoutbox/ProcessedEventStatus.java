package com.saga.common.kafkaoutbox;

/**
 * Outbox event lifecycle state.
 *
 * Maintenance checklist when adding/changing a status:
 * 1. Update claim predicates in ProcessedEventRepositoryCustomImpl#claimBatch.
 * 2. Add or adjust matching indexes in a NEW Flyway migration.
 * 3. Update transition logic in ProcessedEventTxService and ProcessedEventRepository updates.
 */
public enum ProcessedEventStatus {
    NEW,
    IN_PROGRESS,
    RETRY,
    SENT,
    FAILED
}
