package com.saga.order.model.dto;

import java.time.Instant;

public record ErrorResponse(
        String message,
        ErrorCode code,
        Instant timestamp
) {}