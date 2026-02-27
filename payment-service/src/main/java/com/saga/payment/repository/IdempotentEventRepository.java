package com.saga.payment.repository;

import com.saga.common.idempotency.IdempotentEventClaimRepository;
import com.saga.payment.model.entity.IdempotentEvent;

public interface IdempotentEventRepository extends IdempotentEventClaimRepository<IdempotentEvent, Long> {
}
