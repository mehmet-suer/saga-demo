package com.saga.order.repository;

import com.saga.common.idempotency.IdempotentEventClaimRepository;
import com.saga.order.model.entity.IdempotentEvent;

public interface IdempotentEventRepository extends IdempotentEventClaimRepository<IdempotentEvent, Long> {
}
