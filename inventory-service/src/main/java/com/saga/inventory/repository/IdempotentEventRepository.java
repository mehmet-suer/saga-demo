package com.saga.inventory.repository;

import com.saga.common.idempotency.IdempotentEventClaimRepository;
import com.saga.inventory.model.entity.IdempotentEvent;

public interface IdempotentEventRepository extends IdempotentEventClaimRepository<IdempotentEvent, Long> {
}
